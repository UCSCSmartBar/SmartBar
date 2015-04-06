# FileName: DispensingSystem.py
# Description: control software for the smartbar dispensing system
# Author: Brendan Short
# Date: 4/5/2015

# Imports

import MySQLdb
import RPi.GPIO as GPIO
import time
import io
import string
import threading
import WebInv
import sys
sys.path.append("..")
import Printer

# Pin Assignments

ValveController_SR_Data = 3 # pin number of serial output to shift register
ValveController_SR_Clock = 2 # pin number of clock output to shift register
ValveController_SR_Store = 4 # pin number of store data output to shift register
SBDispenser_ATXPin = 19 # pin number to control ATX
SBDispenser_CoolCarbPowerPin = 12 # pin number of relay control to the cooling and carbonation system
SBDispenser_WaterPumpPowerPin = 26 # pin number of relay control to the water pump

'''
 Message Format:
 $(System),(Message Type),(Main Code)@(Message Reason)@(Message Parameters)

# Main Error Codes
 DD : Dispense Drink 

 

'''


# Pin Assignments




ValveController_SR_TotalBits = 24 # number of shift register bits in use (3 shift registers)

WaterOutputValveNumber = 4

CO2WaterOutputValveNumber = 5
SBDispenser_CommandResults = ["Fail","Success"]
SBDispenser_MixType = ["Alcohol","Mixer"]

SBDispenser_LineSplittingCharacter = ","
SBDispenser_PacketSplittingCharacter = "@"

SBDispenser_MinimumContainerContents = .5


######################################################################################        
    
# Class: SmartBar_Dispenser
# Description: The main class that is used by the main system to perform dispenser related tasks with simple function calls.  This class relies on all of the following classes in this module
# Author: Brendan Short
# Last Modified: 2/13/2015

# Error Codes:   Stage of failure, DispenseDrink_Failure returned
#
# -1 ReceoveDrinkOrder_Fail
# -2 ProcessDrinkOrder_Fail

# Success Codes: Allow dispensing functions to progress, DispenseDrink_Success returned upon successful dispense
#
# 1 ReceoveDrinkOrder_Success
# 2 ProcessDrinkOrder_Success




class SmartBar_Dispenser():
    
    # Error Codes:   Stage of failure, DispenseDrink_Failure returned
    
    ReceoveDrinkOrder_Fail = -1
    ProcessDrinkOrder_Fail = -2
    FindDrinkComponent_Fail = -3
    # Success Codes: Allow dispensing functions to progress, DispenseDrink_Success returned upon successful dispense
    
    ReceiveDrinkOrder_Success = 1
    ProcessDrinkOrder_Success = 2



    # Variables
    

    TotalDispensingValves = 22 # number of dispensing valves in use


    OzScalingFactor = 1 # scale up ounces to transfer data more compactly - ie: 1.5oz = 15

    AlcoholDispenseTimePerOz = 3 # time in seconds to dispense one fluid ounce of alcohol

    MixerDispenseTimePerOz = 1.5 # time in seconds to dispense one fluid ounce of mixer - Calculated by time for 1oz of mixer / 1oz mixer + 5 oz water

    WaterValveNumber = 18

    CarbonatedWaterValveNumber = 19

    TimerInterval = .001 # interval between valve timing check routines (while dispensing)

    CurrentlyDispensing = 1 # flag indicating if dispensing

    LineSplittingCharacter = ","

    PacketSplittingCharacter = "@"


    # Functions

    def ReceiveCommand(self, incoming_command):


        SmartBar_Dispenser.CommandStatus = 1
        SmartBar_Dispenser.CommandReturnMessage = "Incomplete"
        SmartBar_Dispenser.CommandPacket = incoming_command # store incoming command packet

        SmartBar_Dispenser.CommandType = incoming_command.split(SmartBar_Dispenser.PacketSplittingCharacter)[0].split(SmartBar_Dispenser.LineSplittingCharacter)[0] # get the incoming command

        SmartBar_Dispenser.PrintFilter.System(("Received Command : "+SmartBar_Dispenser.CommandPacket),SmartBar_Dispenser.PrintFilter.Title)
        SmartBar_Dispenser.PrintFilter.WriteToSystemTextLogFile()
        
        if (SmartBar_Dispenser.CommandType == "$SD"):

            self.Start_Dispensing_System()

        if (SmartBar_Dispenser.CommandType == "$DO"):

            self.DispenseDrinkOrder(SmartBar_Dispenser.CommandPacket)

        elif (SmartBar_Dispenser.CommandType == "$GI"):

            SmartBar_Dispenser.CommandReturnMessage = SmartBar_Dispenser.InventoryManager.GetInventoryString()
            SmartBar_Dispenser.InventoryManager.PrintInventory()

        elif (SmartBar_Dispenser.CommandType == "$IV"):

            SmartBar_Dispenser.CommandReturnMessage = SmartBar_Dispenser.InventoryManager.UserSetInventory(SmartBar_Dispenser.CommandPacket)
            SmartBar_Dispenser.InventoryManager.PrintInventory()
        
        SmartBar_Dispenser.PrintFilter.System((str(SmartBar_Dispenser.CommandType)+" "+SBDispenser_CommandResults[SmartBar_Dispenser.CommandStatus]+" : Return -> "+str(SmartBar_Dispenser.CommandReturnMessage)),SmartBar_Dispenser.PrintFilter.Title)
        SmartBar_Dispenser.PrintFilter.WriteToSystemTextLogFile()
        



    def DispenseDrinkOrder(self,incoming_drink_order): # recieve order from tablet, then analyze and dispense - main function call of this module

        self.ReceiveDrinkOrder(incoming_drink_order)
        if (SmartBar_Dispenser.CommandStatus == 1):
            self.ProcessDrinkOrder()
            if (SmartBar_Dispenser.CommandStatus == 1):
                SmartBar_Dispenser.ValveManager.OpenQueuedValves()
                self.DrinkPouredUpdate()
                SmartBar_Dispenser.CommandReturnMessage = ("$DS,AK,DD@SD")

        return




    def ReceiveDrinkOrder(self, drink_order):

            #PacketFormat:     $(Drink Order).(Number of Alcohols).(Number of Mixers)@
            #                  (Alcohol Type).(Alcohol Brand).(Dispense Volume)@
            #                                            :
            #                  (Mixer Type).(Mixer Brand).(Carbonation).(Dispense Volume)@
            #                                            :

        
        SmartBar_Dispenser.PrintFilter.System("Receive Drink Order",SmartBar_Dispenser.PrintFilter.SubTitle)
        SmartBar_Dispenser.PrintFilter.System("Order String: "+drink_order,SmartBar_Dispenser.PrintFilter.Standard)            

        try: # try to break up the drink order packet and get general drink order info

            self.OrderPacket = drink_order.split(SmartBar_Dispenser.PacketSplittingCharacter) # break up the order into rows to be processed               
            self.GeneralDrinkInfo = self.OrderPacket[0].split(SmartBar_Dispenser.LineSplittingCharacter) # get the general drink info - $DO.X.Y where $DO is the drink order command, X is the number of alcohol components, and Y is the number of conentrate components
            self.NumOfAlcoholComponents = int(self.GeneralDrinkInfo[1]) # number of alcohol components
            self.NumOfMixerComponents = int(self.GeneralDrinkInfo[2]) # number of Mixer components
            SmartBar_Dispenser.PrintFilter.System("# of Alcohols: "+str(self.NumOfAlcoholComponents)+" # of Mixers : "+str(self.NumOfMixerComponents)+" Packet Length : "+str(len(self.OrderPacket)),SmartBar_Dispenser.PrintFilter.Standard)
            self.CurrentDrink = SmartBar_DrinkOrder(self.NumOfAlcoholComponents,self.NumOfMixerComponents) # create a new drink order

            for i in range(self.NumOfAlcoholComponents): # store all alcohol drink components
                self.CurrentDrink.StoreAlcoholComponent(i,self.OrderPacket[i+1]) # store individual alcohol drink component
            for i in range(self.NumOfMixerComponents): # store all Mixer drink components
                self.CurrentDrink.StoreMixerComponent(i,self.OrderPacket[i+1+self.NumOfAlcoholComponents]) # store individual Mixer drink componen

        except: # couldn't break up drink order packet or couldn't get general drink order info
            SmartBar_Dispenser.CommandStatus = 0
       #     SmartBar_Dispenser.CommandReturnMessage = ("$DS,ER,DD@RO@"+drink_order)
            SmartBar_Dispenser.PrintFilter.Error('Invalid drink order packet',SmartBar_Dispenser.PrintFilter.SubTitle)
            SmartBar_Dispenser.PrintFilter.Error(('Input String: '+drink_order),SmartBar_Dispenser.PrintFilter.Standard)
  


    def ProcessDrinkOrder(self):
        
        SmartBar_Dispenser.PrintFilter.System('Processing Order & Loading Valve Queue',SmartBar_Dispenser.PrintFilter.SubTitle)
        self.AlcoholValveDelay = 0
        
        for i in range(self.CurrentDrink.NumberOfAlcoholComponents): # scan through all alcohol components of current drink
            
            if (SmartBar_Dispenser.CommandStatus == 1):
                
                Drink_Category = 0 # Alcohol
                AlcoholType = self.CurrentDrink.Alcohols[i].Type 
                AlcoholBrand = self.CurrentDrink.Alcohols[i].Brand
                DispenseVolume = self.CurrentDrink.Alcohols[i].Volume
                ValveNumber = self.SetupValve(Drink_Category,AlcoholType,AlcoholBrand,0,DispenseVolume)
                self.CurrentDrink.Alcohols[i].ValveNumber = ValveNumber
                
            else:
                return
            
        self.MixerValveDelay = 0

        for i in range(self.CurrentDrink.NumberOfMixerComponents): # scan through all alcohol components of current drink

            if (SmartBar_Dispenser.CommandStatus == 1):           
                Drink_Category = 1
                MixerType = self.CurrentDrink.Mixers[i].Type
                MixerBrand = self.CurrentDrink.Mixers[i].Brand
                MixerCarbonation = self.CurrentDrink.Mixers[i].Carbonated
                DispenseVolume = self.CurrentDrink.Mixers[i].Volume
                ValveNumber = self.SetupValve(Drink_Category,MixerType,MixerBrand,MixerCarbonation,DispenseVolume)        
                self.CurrentDrink.Mixers[i].ValveNumber = ValveNumber
            else:
                return
    

    def SetupValve(self,item_category,item_type,item_brand,item_carbonation,item_volume):

        SmartBar_Dispenser.PrintFilter.System('## Searching for '+str(SBDispenser_MixType[int(item_category)]+' T:'+str(item_type)+' B:'+str(item_brand)),SmartBar_Dispenser.PrintFilter.Standard)
        MixType_Alcohol = 0
        MixType_H2OMixer = 1
        MixType_CO2Mixer = 2
        
        if (item_category == 0):

            for i in range(len(SmartBar_Dispenser.InventoryManager.Alcohols)): # scan through all alcohol in inventory

                if (item_type == SmartBar_Dispenser.InventoryManager.Alcohols[i].Type): # if the alcohol type matches continue

                        if (item_brand == SmartBar_Dispenser.InventoryManager.Alcohols[i].Brand): # if the brand also matches, the component is confirmed

                            if (SmartBar_Dispenser.InventoryManager.Alcohols[i].Active == 1): # if the container is currently in use valve info can be added to the list
                          
                                if ((SmartBar_Dispenser.InventoryManager.Alcohols[i].Volume-SBDispenser_MinimumContainerContents) < item_volume):
                                    
                                    SmartBar_Dispenser.CommandReturnMessage = ("$DS,ER,DD@LV,A,"+str(item_type)+","+str(item_brand))
                                    SmartBar_Dispenser.CommandStatus = 0
                                    return
                      
                                ValveTime = float((float(item_volume/SmartBar_Dispenser.OzScalingFactor)) * SmartBar_Dispenser.AlcoholDispenseTimePerOz) # calculating valve open time
                                ValveNumber = SmartBar_Dispenser.InventoryManager.Alcohols[i].ValveNumber
                                ValveDelay = self.AlcoholValveDelay
                                SmartBar_Dispenser.PrintFilter.System((SBDispenser_MixType[int(item_category)]+' T:'+str(item_type)+' B:'+str(item_brand)+' Found - '+str(ValveTime)+'s to dispense '+str(item_volume)+'oz'),SmartBar_Dispenser.PrintFilter.Standard)
                                SmartBar_Dispenser.ValveManager.QueueValve(ValveNumber, ValveTime, ValveDelay,MixType_Alcohol)

                                if (self.AlcoholValveDelayEnabled == 1):

                                    self.AlcoholValveDelay = self.AlcoholValveDelay + ValveTime

                                return ValveNumber
                                    
                                
            SmartBar_Dispenser.CommandStatus = 0
            SmartBar_Dispenser.CommandReturnMessage = ("$DS,ER,DD@NF,A,"+str(item_type)+","+str(item_brand))
            SmartBar_Dispenser.PrintFilter.Error("Alcohol Drink Component Not Found - Type: "+str(item_type)+" , Brand: "+str(item_brand),SmartBar_Dispenser.PrintFilter.Title)

        if (item_category == 1):
                            
            for i in range(len(SmartBar_Dispenser.InventoryManager.Mixers)): # scan through all mixer in inventory

                    if (item_type == SmartBar_Dispenser.InventoryManager.Mixers[i].Type): # if the mixer type matches continue

                            if (item_brand == SmartBar_Dispenser.InventoryManager.Mixers[i].Brand): # if the brand also matches, the component is confirmed

                                if (SmartBar_Dispenser.InventoryManager.Mixers[i].Active == 1): # if the container is currently in use valve info can be added to the list

                                    if ((SmartBar_Dispenser.InventoryManager.Mixers[i].Volume-SBDispenser_MinimumContainerContents) < item_volume):
                                        SmartBar_Dispenser.CommandReturnMessage = ("$DS,ER,DD@LV,M,"+str(item_type)+","+str(item_brand))
                                        SmartBar_Dispenser.CommandStatus = 0
                                        return
                                     
                                    ValveTime = float((float(item_volume/SmartBar_Dispenser.OzScalingFactor) * SmartBar_Dispenser.MixerDispenseTimePerOz)) # calculating valve open time
                                    ValveNumber = SmartBar_Dispenser.InventoryManager.Mixers[i].ValveNumber
                                    ValveDelay = self.MixerValveDelay
                                    self.MixerValveDelay = self.MixerValveDelay + ValveTime
                                    SmartBar_Dispenser.PrintFilter.System((SBDispenser_MixType[int(item_category)]+' T:'+str(item_type)+' B:'+str(item_brand)+' Found - '+str(ValveTime)+'s to dispense '+str(item_volume)+'oz'),SmartBar_Dispenser.PrintFilter.Standard)

                                    if (item_carbonation == 0):

                                        MixerType = MixType_H2OMixer

                                    if (item_carbonation == 1):
                                        
                                        MixerType = MixType_CO2Mixer

                                    SmartBar_Dispenser.ValveManager.QueueValve(ValveNumber,ValveTime,ValveDelay,MixerType)

                                    return ValveNumber

            SmartBar_Dispenser.CommandStatus = 0
            SmartBar_Dispenser.CommandReturnMessage = ("$DS,ER,DD@NF@M,"+str(item_type)+","+str(item_brand))
            SmartBar_Dispenser.PrintFilter.Error("Mixer Drink Component Not Found - Type: "+str(item_type)+" , Brand: "+str(item_brand),SmartBar_Dispenser.PrintFilter.Title)

            return SmartBar_Dispenser.FindDrinkComponent_Fail
                                                       


        
        

         

        
    
    def __init__(self): ## Sets up an instance of the SmartBar_Dispenser class by initializing gpio, creating instance of the SmartBar_Inventory class

        SmartBar_Dispenser.PrintFilter = Printer.PrinterLogger(1,1,1,1,"Dispensing",2)
        self.Start_Dispensing_System()

    def Start_Dispensing_System(self):

        SmartBar_Dispenser.CommandStatus = 1
        SmartBar_Dispenser.CommandReturnMessage = "Inactive"
        SmartBar_Dispenser.PrintFilter = Printer.PrinterLogger(1,1,1,1,"Dispensing",2)
        SmartBar_Dispenser.PrintFilter.System("Starting Dispenser Initialization",SmartBar_Dispenser.PrintFilter.Title)
        self.GPIO_Initialize() # initialize GPIO
        SmartBar_Dispenser.SystemManager =  PowerSystemManager(SBDispenser_ATXPin,SBDispenser_WaterPumpPowerPin,SBDispenser_CoolCarbPowerPin)
        SmartBar_Dispenser.ValveManager = SmartBar_ValveController(SmartBar_Dispenser.TotalDispensingValves) # initialize valve manager
        SmartBar_Dispenser.InventoryManager = SmartBar_Inventory() # initialize inventory manager
        SmartBar_Dispenser.PrintFilter.System("Dispenser Initialization Complete",SmartBar_Dispenser.PrintFilter.Title)
                                            

   
    def GPIO_Initialize(self): # Sets gpio mode, configures dispensing pins to be outputs
       # WebInv.PutInventory('$IV,2,2@0,WH,1,50.0,59.2@1,GN,1,50.0,59.2@2,GA,1,118.5,128.0@3,TO,1,123.0,128.0')
        SmartBar_Dispenser.PrintFilter.System("Power Control GPIO Initialize",SmartBar_Dispenser.PrintFilter.SubTitle)
        GPIO.setmode(GPIO.BCM) # set GPIO mode
        GPIO.setwarnings(False)                                        
 
    def GPIO_Free(self): # releases all GPIO pins

        GPIO.cleanup() # unexport all GPIO
        
        print ('GPIO Freed')

    def DrinkPouredUpdate(self):
        
        SmartBar_Dispenser.PrintFilter.System('Updating Inventory',SmartBar_Dispenser.PrintFilter.SubTitle)

        for i in range(self.CurrentDrink.NumberOfAlcoholComponents):
            self.ValveNumber = self.CurrentDrink.Alcohols[i].ValveNumber
            self.DispenseQuantity = self.CurrentDrink.Alcohols[i].Volume
            self.VolumeBeforeDispense = SmartBar_Dispenser.InventoryManager.Alcohols[int(self.ValveNumber)].Volume
            SmartBar_Dispenser.InventoryManager.Alcohols[int(self.ValveNumber)].Volume = self.VolumeBeforeDispense-self.DispenseQuantity
            SmartBar_Dispenser.PrintFilter.System(('Alcohol T:'+str(SmartBar_Dispenser.InventoryManager.Alcohols[int(self.ValveNumber)].Type)+' B:'+str(SmartBar_Dispenser.InventoryManager.Alcohols[int(self.ValveNumber)].Brand)+' Updated '+str(self.VolumeBeforeDispense)+'oz ->'+str(SmartBar_Dispenser.InventoryManager.Alcohols[self.ValveNumber].Volume)+'oz'),SmartBar_Dispenser.PrintFilter.Standard)

        for i in range(self.CurrentDrink.NumberOfMixerComponents):
            self.ValveNumber = (self.CurrentDrink.Mixers[i].ValveNumber -len(SmartBar_Dispenser.InventoryManager.Alcohols))
            self.DispenseQuantity = self.CurrentDrink.Mixers[i].Volume
            self.VolumeBeforeDispense = SmartBar_Dispenser.InventoryManager.Mixers[int(self.ValveNumber)].Volume
            SmartBar_Dispenser.InventoryManager.Mixers[int(self.ValveNumber)].Volume = self.VolumeBeforeDispense-self.DispenseQuantity
            SmartBar_Dispenser.PrintFilter.System(('Alcohol T:'+str(SmartBar_Dispenser.InventoryManager.Mixers[int(self.ValveNumber)].Type)+' B:'+str(SmartBar_Dispenser.InventoryManager.Mixers[int(self.ValveNumber)].Brand)+' Updated '+str(self.VolumeBeforeDispense)+'oz ->'+str(SmartBar_Dispenser.InventoryManager.Mixers[self.ValveNumber].Volume)+'oz'),SmartBar_Dispenser.PrintFilter.Standard)      

        SmartBar_Dispenser.InventoryManager.UpdateInventory()



######################################################################################

#Class: SmartBar_DrinkComponent
#Description: A data type to store indiviual valve information for a list in Dispenser
#Author: Brendan Short
#Last Modified: 2/13/2015

        
class ValveDispenseData():

    def __init__(self,valve_number,valve_open_time,delay):

        self.Number = valve_number # valve number 

        self.OpenTime = valve_open_time # dispense time

        self.Delay = delay# delay time


######################################################################################        
            
#Class: SmartBar_DrinkOrder
#Description: A data type to store all information about a drink order
#Author: Brendan Short
#Last Modified: 2/12/2015


class SmartBar_DrinkOrder():
    
    def __init__(self,num_alcohol_components,num_mixer_components): # import properties from drink order

        self.Status = 0 # drink has not yet been poured successfully            
        self.NumberOfAlcoholComponents = num_alcohol_components # store number of alcoholic components 
        self.NumberOfMixerComponents = num_mixer_components # store number of mixer components 
        self.Alcohols = [] # set up list of alcohols to be filled
        self.Mixers = [] # set up list of Mixers to be filled

                                                        
    def StoreAlcoholComponent(self, alcohol_number, order_line): # store alcohol component information

        try: # attempt to store current alcohol data the order
            
            ComponentInfo = order_line.split(SmartBar_Dispenser.LineSplittingCharacter) # break up the string (Alcohol Type).(Alcohol Brand).(Dispense Volume)
            AlcoholType = ComponentInfo[0] # get alcohol type
            AlcoholBrand = int(ComponentInfo[1]) # get alcohol brand
            AlcoholDispenseVolume = float(ComponentInfo[2]) # get alcohol dispense volume
            self.Alcohols.append(SmartBar_DrinkComponent_Alcohol(AlcoholType,AlcoholBrand,AlcoholDispenseVolume)) # store alcohol component data
            SmartBar_Dispenser.PrintFilter.System(('Alcohol Drink Component Stored: T:'+str(AlcoholType)+' B:'+str(AlcoholBrand)+
                                                  ' - '+str(AlcoholDispenseVolume)+'oz'),SmartBar_Dispenser.PrintFilter.Standard)  
        except: # couldn't break up drink order packet or couldn't get general drink order info

            SmartBar_Dispenser.CommandStatus = 0
            SmartBar_Dispenser.CommandReturnMessage = ("$DS,ER,DD@SO@A,"+str(alcohol_number)+","+str(order_line))
            SmartBar_Dispenser.PrintFilter.Error(('Failed to store drink order alcohol component ' +str(alcohol_number)),SmartBar_Dispenser.PrintFilter.SubTitle)
            SmartBar_Dispenser.PrintFilter.Error(('Packet Line: '+order_line),SmartBar_Dispenser.PrintFilter.Standard)
            
            
    def StoreMixerComponent(self, mixer_number, order_line): # store mixer component information

        try:

            ComponentInfo = order_line.split(SmartBar_Dispenser.LineSplittingCharacter) # break up the string (Mixer Type).(Mixer Brand).(Carbonated).(Dispense Volume)
            MixerType = ComponentInfo[0] # get Mixer type
            MixerBrand = int(ComponentInfo[1]) # get Mixer brand
            MixerCarbonation = int(ComponentInfo[2]) # get carbonation choice
            MixerDispenseVolume = float(ComponentInfo[3]) # get mixer  dispense volume
            self.Mixers.append(SmartBar_DrinkComponent_Mixer(MixerType,MixerBrand,MixerCarbonation,MixerDispenseVolume)) # store Mixer component data
            SmartBar_Dispenser.PrintFilter.System(('Mixer Drink Component Stored: T:'+str(MixerType)+' B:'+str(MixerBrand)+
                                                  ' - '+str(MixerDispenseVolume)+'oz'),SmartBar_Dispenser.PrintFilter.Standard)  
        except:
            
            SmartBar_Dispenser.CommandStatus = 0
            SmartBar_Dispenser.CommandReturnMessage = ("$DS,ER,DD@RO@M,"+str(mixer_number)+","+str(order_line))
            SmartBar_Dispenser.PrintFilter.Error(('Failed to store drink order mixer component ' +str(mixer_number)),SmartBar_Dispenser.PrintFilter.SubTitle)
            SmartBar_Dispenser.PrintFilter.Error(('Packet Line: '+order_line),SmartBar_Dispenser.PrintFilter.Standard)

        
    def PrintDrinkOrderData(self): # print out the drink order stored

        print("Alcohol Components:"+str(len(self.Alcohols)))
        for i in range(len(self.Alcohols)):
            print(str(i+1)+' - Type(#): '+str(self.Alcohols[i].Type)+' ;  Brand(#): '+str(self.Alcohols[i].Brand)+' ;  DispenseVolume: '+str(self.Alcohols[i].Volume))
        print("Mixer Components:"+str(len(self.Mixers)))   
        for i in range(len(self.Mixers)):
            print(str(i+1)+' - Type(#): '+str(self.Mixers[i].Type)+' ;  Brand(#): '+str(self.Mixers[i].Brand)+' ;  Carbonated: '+str(self.Mixers[i].Carbonated)+' ;  DispenseVolume: '+str(self.Mixers[i].Volume))
                

        
#Class: SmartBar_DrinkComponent_Alcohol
#Description: A data type to store the alcohol components of a drink order, used by the drink order class
#Author: Brendan Short
#Last Modified: 2/12/2015

class SmartBar_DrinkComponent_Alcohol():

    def __init__(self,alcohol_type,alcohol_brand,dispense_volume): # import properties from drink order

        self.Type = alcohol_type # alcohol type to be dispensed (#) - ie: tequila = 1, rum = 2, vodka      
        self.Brand = alcohol_brand # alcohol brand to be dispensed (#) - ie: grey goose = 1, smirnoff = 2 
        self.Volume = dispense_volume # amount to be dispensed (1/10th oz)
        self.ValveNumber = -1

#Class: SmartBar_DrinkComponent_Mixer
#Description: A data type to store the alcohol components of a drink order, used by the drink order class
#Author: Brendan Short
#Last Modified: 2/12/2015

class SmartBar_DrinkComponent_Mixer():

    def __init__(self,Mixer_type,Mixer_brand,carbonated,dispense_volume): # import properties from drink order

        self.Type = Mixer_type # Mixer type to be dispensed (#) - soda = 1,juice = 2, energy drink = 3
        self.Brand = Mixer_brand # Mixer brand to be dispensed (#) - ie: coke = 1, 7up = 2, fanta = 3
        self.Carbonated = carbonated # carbonated or non-carbonated mixture
        self.Volume = dispense_volume # amount to be dispensed (1/10th oz)
        self.ValveNumber = -1
        
#Class: SmartBar_Inventory
#Description: A class which stores and manages all inventory data
#Author: Brendan Short
#Last Modified: 2/12/2015
        
#SBDispenser_LineSplittingCharacter = ","

#SBDispenser_PacketSplittingCharacter = "@"

class SmartBar_Inventory():

    def __init__(self):

        SmartBar_Inventory.Alcohols = [] # set up list of alcohols to be filled
        SmartBar_Inventory.Mixers = [] # set up list of Mixers to be filled
        SmartBar_Dispenser.PrintFilter.System("Importing Inventory",SmartBar_Dispenser.PrintFilter.SubTitle)
        self.ImportInventory()
        
    def ImportInventory(self):
        
        try:
            self.WebInventory = WebInv.GetInventory()
        except:
            SmartBar_Dispenser.CommandReturnMessage = ("$DS,ER,SS@II")
            SmartBar_Dispenser.PrintFilter.Error("Failed to Get Inventory from Web Server",SmartBar_Dispenser.PrintFilter.Title)
            SmartBar_Dispenser.CommandStatus = 0
            return
        try:
            self.StoredInventory = (self.WebInventory).split(SBDispenser_PacketSplittingCharacter)
            self.CurrentLine = (self.StoredInventory[0]).split(SBDispenser_LineSplittingCharacter)
            self.NextLineToRead = 1
            SmartBar_Inventory.NumberOfAlcohols = int(self.CurrentLine[1])
            SmartBar_Inventory.NumberOfMixers = int(self.CurrentLine[2])
            SmartBar_Dispenser.PrintFilter.System,("Inventory Received from Web Server - Mixers: "+str(SmartBar_Inventory.NumberOfMixers)+"  - Alcohols: "+str(SmartBar_Inventory.NumberOfAlcohols)+'\n',SmartBar_Dispenser.PrintFilter.Standard)
            self.ImportInventory_Alcohols()
            self.ImportInventory_Mixers()
        except:
            SmartBar_Dispenser.PrintFilter.Error("Invalid Inventory Received from Web Server",SmartBar_Dispenser.PrintFilter.Title)
            SmartBar_Dispenser.CommandStatus = 0
            SmartBar_Dispenser.CommandReturnMessage = ("$DS,ER,SS@II")
        

    def ImportInventory_Mixers(self):

        for i in range(SmartBar_Inventory.NumberOfMixers):

            self.CurrentLine = self.StoredInventory[self.NextLineToRead].split(SBDispenser_LineSplittingCharacter)                        
            self.NextLineToRead = self.NextLineToRead + 1
            MixerValveNumber = int(self.CurrentLine[0])
            MixerType = self.CurrentLine[1]
            MixerBrand = int(self.CurrentLine[2])
            MixerVolume = float(self.CurrentLine[3])
            MixerFullVolume = float(self.CurrentLine[4])
            MixerActive = 1
            MixerName = "Need Import"
            MixerCarbonation = "Need Import"
            SmartBar_Inventory.Mixers.append(SmartBar_Inventory_Mixer(MixerActive,MixerValveNumber,MixerName,MixerType,
                                                                                  MixerBrand,MixerCarbonation,MixerVolume,MixerFullVolume)) # store Mixer inventory data
            SmartBar_Dispenser.PrintFilter.System('Mixer'+str(i+1)+' ;  Valve # '
                  +str(SmartBar_Inventory.Mixers[i].ValveNumber)+' ;  Type(#): '+str(SmartBar_Inventory.Mixers[i].Type)+
                  ' ;  Brand(#): '+str(SmartBar_Inventory.Mixers[i].Brand)+' ;  Volume: '+str(SmartBar_Inventory.Mixers[i].Volume),
                                                  SmartBar_Dispenser.PrintFilter.Standard)

            
    def ImportInventory_Alcohols(self):

        for i in range(SmartBar_Inventory.NumberOfAlcohols):

            self.CurrentLine = self.StoredInventory[self.NextLineToRead].split(SBDispenser_LineSplittingCharacter)                    
            self.NextLineToRead = self.NextLineToRead + 1
            AlcoholValveNumber = int(self.CurrentLine[0])
            AlcoholType = self.CurrentLine[1]
            AlcoholBrand = int(self.CurrentLine[2])
            AlcoholVolume = float(self.CurrentLine[3])
            AlcoholFullVolume = float(self.CurrentLine[4]) # Total volume of alcohol container - ie: Handle full volume = 1.75 liters (59.2 fl oz)
            AlcoholActive = 1
            AlcoholName = "Need Import"
            SmartBar_Inventory.Alcohols.append(SmartBar_Inventory_Alcohol(AlcoholActive,AlcoholValveNumber,AlcoholName,AlcoholType,
                                                                          AlcoholBrand,AlcoholVolume,AlcoholFullVolume)) # store Alcohol inventory data      
            SmartBar_Dispenser.PrintFilter.System('Alcohol'+str(i+1)+' ;  Valve # '
                  +str(SmartBar_Inventory.Alcohols[i].ValveNumber)+' ;  Type: '+str(SmartBar_Inventory.Alcohols[i].Type)+
                  ' ;  Brand(#): '+str(SmartBar_Inventory.Alcohols[i].Brand)+' ;  Volume: '+str(SmartBar_Inventory.Alcohols[i].Volume),
                                                  SmartBar_Dispenser.PrintFilter.Standard)

    def UpdateInventory(self):
    
        self.UpdateInventoryString = SBDispenser_LineSplittingCharacter.join(['$IV',str(len(SmartBar_Dispenser.InventoryManager.Alcohols)),
                           str(len(SmartBar_Dispenser.InventoryManager.Mixers))])
        for i in range(len(SmartBar_Dispenser.InventoryManager.Alcohols)):
            ItemString = SBDispenser_LineSplittingCharacter.join([str(SmartBar_Dispenser.InventoryManager.Alcohols[i].ValveNumber),
                               str(SmartBar_Dispenser.InventoryManager.Alcohols[i].Type),
                               str(SmartBar_Dispenser.InventoryManager.Alcohols[i].Brand),
                               str(SmartBar_Dispenser.InventoryManager.Alcohols[i].Volume),
                               str(SmartBar_Dispenser.InventoryManager.Alcohols[i].FullVolume)])
            self.UpdateInventoryString = SBDispenser_PacketSplittingCharacter.join([self.UpdateInventoryString,ItemString])
            
        for i in range(len(SmartBar_Dispenser.InventoryManager.Mixers)):
            ItemString = SBDispenser_LineSplittingCharacter.join([str(SmartBar_Dispenser.InventoryManager.Mixers[i].ValveNumber),
                               str(SmartBar_Dispenser.InventoryManager.Mixers[i].Type),
                               str(SmartBar_Dispenser.InventoryManager.Mixers[i].Brand),
                               str(SmartBar_Dispenser.InventoryManager.Mixers[i].Volume),
                               str(SmartBar_Dispenser.InventoryManager.Mixers[i].FullVolume)])
            self.UpdateInventoryString = SBDispenser_PacketSplittingCharacter.join([self.UpdateInventoryString,ItemString])
            
        WebInv.PutInventory(self.UpdateInventoryString)                       

    def GetInventoryString(self):
        
        self.ReturnInventoryString = SBDispenser_LineSplittingCharacter.join(['$IV',str(len(SmartBar_Dispenser.InventoryManager.Alcohols)),
                           str(len(SmartBar_Dispenser.InventoryManager.Mixers))])
        for i in range(len(SmartBar_Dispenser.InventoryManager.Alcohols)):
            ItemString = SBDispenser_LineSplittingCharacter.join([str(SmartBar_Dispenser.InventoryManager.Alcohols[i].ValveNumber),
                               str(SmartBar_Dispenser.InventoryManager.Alcohols[i].Type),
                               str(SmartBar_Dispenser.InventoryManager.Alcohols[i].Brand),
                               str(SmartBar_Dispenser.InventoryManager.Alcohols[i].Volume),
                               str(SmartBar_Dispenser.InventoryManager.Alcohols[i].FullVolume)])
            self.ReturnInventoryString = SBDispenser_PacketSplittingCharacter.join([self.ReturnInventoryString,ItemString])
            
        for i in range(len(SmartBar_Dispenser.InventoryManager.Mixers)):
            ItemString = SBDispenser_LineSplittingCharacter.join([str(SmartBar_Dispenser.InventoryManager.Mixers[i].ValveNumber),
                               str(SmartBar_Dispenser.InventoryManager.Mixers[i].Type),
                               str(SmartBar_Dispenser.InventoryManager.Mixers[i].Brand),
                               str(SmartBar_Dispenser.InventoryManager.Mixers[i].Volume),
                               str(SmartBar_Dispenser.InventoryManager.Mixers[i].FullVolume)])
            self.ReturnInventoryString = SBDispenser_PacketSplittingCharacter.join([self.ReturnInventoryString,ItemString])

        return self.ReturnInventoryString

    def PrintInventory(self):
        
        for i in range(len(SmartBar_Dispenser.InventoryManager.Alcohols)):
            SmartBar_Dispenser.PrintFilter.System((SmartBar_Inventory.Alcohols[i].Name+' ;Valve#'
                  +str(SmartBar_Inventory.Alcohols[i].ValveNumber)+' ;Type: '+str(SmartBar_Inventory.Alcohols[i].Type)+
                  ' ;Brand: '+str(SmartBar_Inventory.Alcohols[i].Brand)+' ;Volume: '+str(SmartBar_Inventory.Alcohols[i].Volume)+'oz/'+ str(SmartBar_Inventory.Alcohols[i].FullVolume)+'oz'),
                                                  SmartBar_Dispenser.PrintFilter.Standard)
        for i in range(len(SmartBar_Dispenser.InventoryManager.Mixers)):
            SmartBar_Dispenser.PrintFilter.System((SmartBar_Inventory.Mixers[i].Name+' ;Valve#'
                  +str(SmartBar_Inventory.Mixers[i].ValveNumber)+' ;Type: '+str(SmartBar_Inventory.Mixers[i].Type)+
                  ' ;Brand: '+str(SmartBar_Inventory.Mixers[i].Brand)+' ;Volume: '+str(SmartBar_Inventory.Mixers[i].Volume)+'oz/'+ str(SmartBar_Inventory.Mixers[i].FullVolume)+'oz'),
                                                  SmartBar_Dispenser.PrintFilter.Standard)

    def UserSetInventory(self,inventory_string):
        
        WebInv.PutInventory(inventory_string)
        self.ImportInventory()

        

        
##    def DrinkPouredUpdate(self):
##        
##        for i in range(SmartBar_Dispenser.CurrentDrink.Alcohols):
##            ValveNumber = SmartBar_Dispenser.CurrentDrink.Alcohols[i].ValveNumber
##            DispenseQuantity = SmartBar_Dispenser.CurrentDrink.Alcohols[i].Volume
##            VolumeBeforeDispense = SmartBar_Inventory.Alcohols[ValveNumber].Volume
##            SmartBar_Inventory.Alcohols[ValveNumber].Volume = VolumeBeforeDispense-DispenseQuantity
##            SmartBar_Dispenser.PrintFilter.System(('Alcohol T:'+str(SmartBar_Inventory.Alcohols[ValveNumber].Type)+' B:'+str(SmartBar_Inventory.Alcohols[ValveNumber].Brand)+' Updated '+str(VolumeBeforeDispense)+'->'+str(SmartBar_Inventory.Alcohols[ValveNumber].Volume)),SmartBar_Dispenser.PrintFilter.Standard)

   #     for i in range SmartBar_Dispenser.CurrentDrink.NumberOfAlcoholComponents

     #       SmartBar_Dispenser.CurrentDrink.
    
#Class: SmartBar_Inventory_Mixer
#Description: A data type to store the properties of individual Mixers in the SmartBar inventory
#Author: Brendan Short
#Last Modified: 2/12/2015

class SmartBar_Inventory_Mixer(): # class to store individual Mixer properties

    def __init__(self,Mixer_active,valve_number,Mixer_name,Mixer_type,Mixer_brand,Mixer_carbonation,Mixer_volume,full_volume): # import properties

        self.Active = Mixer_active # flag indicating if Mixer is available to be mixed - in stock
        self.ValveNumber = valve_number # number of the valve connected to the corresponding dispensing pump     
        self.Name = Mixer_name # Mixer name - ie: "Canada Dry Ginger Ale"
        self.Type = Mixer_type # Mixer type (#) - ie: soda = 1,juice = 2, energy drink = 3  
        self.Brand = Mixer_brand # Mixer brand (#)  - ie: coke = 1, 7up = 2, fanta = 3
        self.Carbonated = Mixer_carbonation # carbonated water normally mixed in or not - ie: sodas = 1, juices = 0
        self.Volume = Mixer_volume # amount of Mixer remaining
        self.FullVolume = full_volume 
            

#Class: SmartBar_Inventory_Alcohol
#Description: A data type to store the properties of individual alcohols in the SmartBar inventory
#Author: Brendan Short
#Last Modified: 2/12/2015
        
class SmartBar_Inventory_Alcohol(): 
    
    def __init__(self,alcohol_active,valve_number,alcohol_name,alcohol_type,alcohol_brand,alcohol_volume,full_volume): # import properties

        self.Active = alcohol_active # flag indicating if Mixer is available to be mixed - in stock
        self.ValveNumber = valve_number # number of the valve connected to the corresponding dispensing pump
        self.Name = alcohol_name # alcohol name - ie: "Smirnoff Vodka"
        self.Type = alcohol_type # alcohol type (#) - ie: tequila = 1, rum = 2, vodka
        self.Brand = alcohol_brand # alcohol brand (#) - ie: grey goose = 1, smirnoff = 2
        self.Volume = alcohol_volume # amount of alcohol remaining
        self.FullVolume = full_volume 


#Class: SmartBar_Valve Controller
#Description: A class which stores all of the current valve states (open/close) and controls the shift registers to open and close the valves
#Author: Brendan Short
#Last Modified: 2/12/2015
        
class SmartBar_ValveController(): 

    def __init__(self,total_valves): # set up gpio, set up variables based on the total amount of valves being used
        

        # Additional Variable Setup
        self.AlcoholValveDelayEnabled = 1
        self.ValveHasNotOpened = 0
        self.ValveHasOpened = 1
        self.ValveOpen = 1
        self.AlcoholValveDelayEnabled = 1
        self.SR_ClockPause = .0000001# time between high and low clock signal to shift register
        self.WaterValveNumber = WaterOutputValveNumber

        self.CarbonatedWaterValveNumber = CO2WaterOutputValveNumber

        self.TotalValves = total_valves # total number of valves in use

        self.CurrentValveState = [0] * total_valves # state (open/closed) of all of the valves, initialized to 0 (closed)

        self.QueuedValves = []

        # GPIO Setup

        self.SR_Data = ValveController_SR_Data # pin of serial output to shift register  
        self.SR_Clock = ValveController_SR_Clock # pin of clock to shift register
        self.SR_Store = ValveController_SR_Store # pin of store data to shift register   
        self.SR_ClockPause = self.SR_ClockPause # time between high and low clock signal to shift register
        self.SR_TotalBits = ValveController_SR_TotalBits # number of shift register bits in use (3 shift registers)
        GPIO.setup(self.SR_Data,GPIO.OUT)  # set the shift register serial data pin output      
        GPIO.output(self.SR_Data,GPIO.LOW) # set the shift register serial data pin low initially       
        GPIO.setup(self.SR_Clock,GPIO.OUT) # set the shift register clock pin as output
        GPIO.output(self.SR_Clock,GPIO.LOW) # set the shift register clock pin low initially
        GPIO.setup(self.SR_Store,GPIO.OUT) # set the shift register store pin as output
        GPIO.output(self.SR_Store,GPIO.LOW) # set the shift register store pin low initially
        self.ShiftRegisterClear()

        SmartBar_Dispenser.PrintFilter.System('GPIO Initialized: Shift Register Control',SmartBar_Dispenser.PrintFilter.Standard) # system print statement 
        

    def OpenQueuedValves(self):
        
        self.MaxDispenseTime =  self.MaxQueuedValveTime() + .1
        
        self.DispenseStartTime = time.time()

        self.DispenseEndTime = self.DispenseStartTime + self.MaxDispenseTime

        SmartBar_Dispenser.PrintFilter.System('Valve Controller Active - Dispense Time: '+str(self.MaxQueuedValveTime()), SmartBar_Dispenser.PrintFilter.SubTitle)

        self.CurrentDispenseTime = time.time()

        while(self.CurrentDispenseTime < self.DispenseEndTime):

            self.CurrentDispenseTime = time.time()

            self.ValveMonitor()
        
        self.UpdateShiftRegisters()


    def ValveMonitor(self):

        Changes = 0

        for i in range(len(self.QueuedValves)):

            if (  (self.CurrentDispenseTime >= (self.QueuedValves[i].Delay + self.QueuedValves[i].OpenTime + self.DispenseStartTime)) & (self.QueuedValves[i].CurrentlyOpen == 1)):

                SmartBar_Dispenser.PrintFilter.System("valve # "+str(self.QueuedValves[i].ValveNumber)+" closed  @ dispense time: "+str(self.CurrentDispenseTime-self.DispenseStartTime)+" seconds", SmartBar_Dispenser.PrintFilter.Standard)

                self.QueuedValves[i].CurrentlyOpen = 0

                self.CurrentValveState[self.QueuedValves[i].ValveNumber] = 0

                if (self.QueuedValves[i].Mix == 1):
                    
                    SmartBar_Dispenser.PrintFilter.System("valve # "+str(self.WaterValveNumber)+" closed  @ dispense time: "+str(self.CurrentDispenseTime-self.DispenseStartTime)+" seconds", SmartBar_Dispenser.PrintFilter.Standard)

                    self.CurrentValveState[self.WaterValveNumber] = 0

                if (self.QueuedValves[i].Mix == 2):

                    SmartBar_Dispenser.PrintFilter.System("valve # "+str(self.CarbonatedWaterValveNumber)+" closed  @ dispense time: "+str(self.CurrentDispenseTime-self.DispenseStartTime)+" seconds", SmartBar_Dispenser.PrintFilter.Standard)

                    self.CurrentValveState[self.CarbonatedWaterValveNumber] = 0

                Changes = 1
                

            if ((self.CurrentDispenseTime >= (self.QueuedValves[i].Delay + self.DispenseStartTime)) & (self.QueuedValves[i].HasBeenOpened != 1)):

                SmartBar_Dispenser.PrintFilter.System("valve # "+str(self.QueuedValves[i].ValveNumber)+" opened  @ dispense time: "+str(self.CurrentDispenseTime-self.DispenseStartTime)+" seconds", SmartBar_Dispenser.PrintFilter.Standard)

                self.QueuedValves[i].HasBeenOpened = 1

                self.QueuedValves[i].CurrentlyOpen = 1

                self.CurrentValveState[self.QueuedValves[i].ValveNumber] = 1

                if (self.QueuedValves[i].Mix == 1):

                    SmartBar_Dispenser.PrintFilter.System("valve # "+str(self.WaterValveNumber)+" opened  @ dispense time: "+str(self.CurrentDispenseTime-self.DispenseStartTime)+" seconds", SmartBar_Dispenser.PrintFilter.Standard)

                    self.CurrentValveState[self.WaterValveNumber] = 1

                if (self.QueuedValves[i].Mix == 2):

                    SmartBar_Dispenser.PrintFilter.System("valve # "+str(self.CarbonatedWaterValveNumber)+" opened  @ dispense time: "+str(self.CurrentDispenseTime-self.DispenseStartTime)+" seconds", SmartBar_Dispenser.PrintFilter.Standard)

                    self.CurrentValveState[self.CarbonatedWaterValveNumber] = 1

                Changes = 1
                

        if (Changes == 1):

            self.UpdateShiftRegisters()
        

    def OpenValve(self,valve_number): # open a single valve for an indefininte amount of time

        self.CurrentValveState[valve_number] = 1 # valve state open

        self.UpdateShiftRegisters() # update registers to current state
    


##    def OpenValveTimed(self,valve_number,valve_timing, valve_delay): # open a single valve for an set amount of time
##
##        
##
##        ValveToOpen = ValveDispenseData(valve_number,ValveClosed, valve_timing, valve_delay)
##
##        self.QueuedValveTime[valve_number] = valve_timing # valve open time set
##
##        self.UpdateShiftRegisters() # update registers to current state


    def QueueValve(self,valve_number,valve_timing,valve_delay,mix_type): # setup a single valve to be open for some amount of time

        try:

            self.QueuedValves.append(ValveDispenseData(valve_number,self.ValveClosed, self.ValveHasNotOpened, valve_timing, valve_delay,mix_type))

            SmartBar_Dispenser.PrintFilter.System('Valve #'+str(valve_number)+' set for '+str(valve_timing)+' seconds',SmartBar_Dispenser.PrintFilter.Standard)

            return 1

        except:

            SmartBar_Dispenser.PrintFilter.Error('Unable to set Valve #'+str(valve_number)+' for '+str(valve_timing)+' seconds',SmartBar_Dispenser.PrintFilter.Standard)

            return -1

            

    def CloseValve(self,valve_number): # close a single valve

        if (SmartBar_ValveController.ValveDebug == 1):

            print('Valve: '+str(valve_number)+' closed')

        self.QueuedValveState[valve_number] = 0 # valve state closed

        self.UpdateShiftRegisters() # update registers to current state



    def CloseAllValves(self): # close all valves
        
        self.ValveState = [0] * self.TotalValves # all valve states closed

        self.UpdateShiftRegisters() # update registers to current state


    def CheckValves(self,timer_interval):

        for i in range(len(SmartBar_ValveController.CurrentOpenValves)):

            if (self.ValveTimer[SmartBar_ValveController.CurrentOpenValves[i]] > 0):

                self.ValveTimer[SmartBar_ValveController.CurrentOpenValves[i]] = self.ValveTimer[SmartBar_ValveController.CurrentOpenValves[i]] - timer_interval

            if ((self.ValveTimer[SmartBar_ValveController.CurrentOpenValves[i]] <= 0) & (self.ValveState[SmartBar_ValveController.CurrentOpenValves[i]] == 1)):

                self.CloseValve(SmartBar_ValveController.CurrentOpenValves[i])

        
        
    def UpdateShiftRegisters(self): # update shift registers by shifting in state data

        
        i = self.SR_TotalBits - 1 # index for the total number of bits available to the shift register
        
        while(i >= 0): # counting down because the last values are shifted in first
        
            if (i < self.TotalValves): # set all the bits that are being used to control valves
                
                if (self.CurrentValveState[i] == 1): # write a high if the valve is supposed to be open
                    
                    GPIO.output(self.SR_Data,GPIO.HIGH) 
                    
                else: # write a low if the valve is not supposed to be open
                  
                    GPIO.output(self.SR_Data,GPIO.LOW) 
                    
            else: # write low to all of the bits that aren't being used

                GPIO.output(self.SR_Data,GPIO.LOW) # write low to all of the bits that aren't being used

            i = i-1

            self.ShiftRegisterTick() # clock cycle to shift the bits along

        self.ShiftRegisterStore()

 #       SmartBar_ValveController.ShiftRegisterTick(self) # extra clock cycle at the end because shift register clock and read clock are tied together which results in the shift register being one cycle ahead of storage register

            
        
    def ShiftRegisterTick(self): # shift register clock cycle
        
        GPIO.output(self.SR_Clock,GPIO.HIGH) # set rising clock edge

        CurrentClockTime = time.time()

        EndClockTime = (time.time() + self.SR_ClockPause)

        while(CurrentClockTime < EndClockTime):

            CurrentClockTime = time.time()
        
        GPIO.output(self.SR_Clock,GPIO.LOW) # set falling clock edge
        

    def ShiftRegisterStore(self): # shift register clock cycle
        
        GPIO.output(self.SR_Store,GPIO.HIGH) # set rising clock edge
        
        CurrentClockTime = time.time()

        EndClockTime = (time.time() + self.SR_ClockPause)

        while(CurrentClockTime < EndClockTime):

            CurrentClockTime = time.time()
        
        GPIO.output(self.SR_Store,GPIO.LOW) # set falling clock edge
    


    def ShiftRegisterClear(self): # clear shift registers - set all bits to 0

        GPIO.output(self.SR_Data,GPIO.LOW) # shift register serial input set low
        
        for i in range(self.SR_TotalBits): # shift in all low values
            
	    self.ShiftRegisterTick() # shift clock cycle

	self.ShiftRegisterStore() # required extra clock cycle


    def MaxQueuedValveTime(self):

        if (self.QueuedValves == None):

            SmartBar_Dispenser.PrintFilter.Warn('Attempted to get maximum queued valve time of empty queue',SmartBar_Dispenser.PrintFilter.Standard)

            return -1

        else:

            MaxValveTime = 0

            for i in range(len(self.QueuedValves)):

                if ((self.QueuedValves[i].OpenTime + self.QueuedValves[i].Delay) > MaxValveTime):

                    MaxValveTime = (self.QueuedValves[i].OpenTime + self.QueuedValves[i].Delay)

            return MaxValveTime


    def ClearQueuedValves(self):
        
        del self.QueuedValves

        self.QueuedValves = []

        SmartBar_Dispenser.PrintFilter.System('Valve queue cleared',SmartBar_Dispenser.PrintFilter.Standard)


    def TestAllValves(self):

        TestPause = .7
        self.ShiftRegisterClear()
        self.ClearQueuedValves()
        (self.TotalValves) = 12
        for i in range (self.TotalValves):

            self.CurrentValveState[i] = 1

            self.UpdateShiftRegisters()

            CurrentClockTime = time.time()

            EndClockTime = (time.time() + TestPause)

            while(CurrentClockTime < EndClockTime):

                CurrentClockTime = time.time()

            self.CurrentValveState[i] = 0

        self.ShiftRegisterClear()

            

######################################################################################

#Class: SmartBar_DrinkComponent
#Description: A data type to store indiviual valve information for a list in Dispenser
#Author: Brendan Short
#Last Modified: 2/13/2015

        
class ValveDispenseData():

    def __init__(self,valve_number,currently_open, been_opened, valve_open_time,valve_delay,mix_type):

        self.ValveNumber = valve_number # valve number

        self.CurrentlyOpen = currently_open # keeps track of if valve currently open or closed

        self.HasBeenOpened = been_opened

        self.OpenTime = valve_open_time # dispense time

        self.Delay = valve_delay # delay time

        self.Mix = mix_type # mix with carbonated water or water or nothing



class PowerSystemManager():

    def __init__(self, atx_pin, water_pump_pin, cooling_system_pin):

        self.ATXPin = atx_pin
        self.CoolingSystemPin = cooling_system_pin
        self.WaterPumpPin = water_pump_pin

        GPIO.setup(self.ATXPin,GPIO.OUT) # set cooling/carbonation system control pin as an output
        GPIO.output(self.ATXPin,GPIO.HIGH) # set cooling/carbonation control pin low - cooling/carbonation system off - MUST REMAIN OFF UNTIL WATER PUMP HAS STARTED
            
        GPIO.setup(self.CoolingSystemPin,GPIO.OUT) # set cooling/carbonation system control pin as an output
        GPIO.output(self.CoolingSystemPin,GPIO.HIGH) # set cooling/carbonation control pin low - cooling/carbonation system off - MUST REMAIN OFF UNTIL WATER PUMP HAS STARTED
        
        GPIO.setup(self.WaterPumpPin,GPIO.OUT) # set water pump control pin as an output
        GPIO.output(self.WaterPumpPin,GPIO.LOW) # set water pump control pin low - water pump off                                    


    def TurnATXOnOff(self,on_off):

        if (on_off == 1):
            GPIO.output(self.ATXPin,GPIO.HIGH)
            SmartBar_Dispenser.PrintFilter.System('ATX Turned On',SmartBar_Dispenser.PrintFilter.Standard) 

        else:
            GPIO.output(self.ATXPin,GPIO.LOW)
            SmartBar_Dispenser.PrintFilter.System('ATX Turned Off',SmartBar_Dispenser.PrintFilter.Standard) 



    def TurnWaterPumpOnOff(self,on_off):

        if (on_off == 1):
            GPIO.output(self.WaterPumpPin,GPIO.LOW)
            SmartBar_Dispenser.PrintFilter.System('Water Pump Turned On',SmartBar_Dispenser.PrintFilter.Standard) 

        else:
            GPIO.output(self.WaterPumpPin,GPIO.HIGH)
            SmartBar_Dispenser.PrintFilter.System('Water Pump Turned Off',SmartBar_Dispenser.PrintFilter.Standard) 



    def TurnCoolingSystemOnOff(self,on_off):

        if (on_off == 1):
            GPIO.output(self.CoolingSystemPin,GPIO.HIGH)
            SmartBar_Dispenser.PrintFilter.System('Water Pump Turned On',SmartBar_Dispenser.PrintFilter.Standard) 

        else:
            GPIO.output(self.CoolingSystemPin,GPIO.LOW)
            SmartBar_Dispenser.PrintFilter.System('Water Pump Turned Off',SmartBar_Dispenser.PrintFilter.Standard) 


            



