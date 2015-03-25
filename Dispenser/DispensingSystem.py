# !/home/pi/Desktop/SmartBar
# FileName: DispensingPrototype.py
# Description: A test driver for the dispensing system
# Author: Brendan Short
# Date: 2/13/2015

import RPi.GPIO as GPIO
import time
import io
import string
import threading



ValveOpen = 1

ValveClosed = 0
 
ValveHasNotOpened = 0

ValveHasOpened = 1

AlcoholValveDelayEnabled = 1

ValveController_SR_Data = 5 # pin number of serial output to shift register
    
ValveController_SR_Clock = 13 # pin number of clock output to shift register

ValveController_SR_Store = 6 # pin number of store data output to shift register
    
ValveController_SR_ClockPause = .0000001# time between high and low clock signal to shift register

ValveController_SR_TotalBits = 24 # number of shift register bits in use (3 shift registers)

WaterOutputValveNumber = 4

CO2WaterOutputValveNumber = 5
SBDispenser_CommandResults = ["Fail","Success"]
SBDispenser_MixType = ["Alcohol","Mixer"]
SBDispenser_CurrentInventoryFilePath = "UCSC_SmartBar_InventoryStatus.txt"

SBDispenser_SystemDataLogFilePath = "Logs/UCSC_SmartBar_DataSystemLog.txt"
SBDispenser_SystemTextLogFilePath = "Logs/UCSC_SmartBar_TextSystemLog.txt"
SBDispenser_ErrorDataLogFilePath = "Logs/UCSC_SmartBar_DataErrorLog.txt"
SBDispenser_ErrorTextLogFilePath = "Logs/UCSC_SmartBar_TextErrorLog.txt"
SBDispenser_FullDataLogFilePath = "Logs/UCSC_SmartBar_DataFullLog.txt"
SBDispenser_FullTextLogFilePath = "Logs/UCSC_SmartBar_TextFullLog.txt"
SBDispenser_DrinkDataLogFilePath = "Logs/UCSC_SmartBar_DataDrinkLog.txt"
SBDispenser_DrinkTextLogFilePath = "Logs/UCSC_SmartBar_TextDrinkLog.txt"

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
    
    CoolCarbPower = 21 # pin number of relay control to the cooling and carbonation system
    
    WaterPumpPower = 26 # pin number of relay control to the water pump

    TotalDispensingValves = 22 # number of dispensing valves in use


    OzScalingFactor = 1 # scale up ounces to transfer data more compactly - ie: 1.5oz = 15

    AlcoholDispenseTimePerOz = 4 # time in seconds to dispense one fluid ounce of alcohol

    MixerDispenseTimePerOz = 5/6 # time in seconds to dispense one fluid ounce of mixer - Calculated by time for 1oz of mixer / 1oz mixer + 5 oz water

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
        
        if (SmartBar_Dispenser.CommandType == "$DO"):

            self.DispenseDrinkOrder(SmartBar_Dispenser.CommandPacket)

        elif (SmartBar_Dispenser.CommandType == "$GI"):

            SmartBar_Dispenser.CommandReturnMessage = SmartBar_Dispenser.InventoryManager.GetInventoryString()
            SmartBar_Dispenser.InventoryManager.PrintInventory()

        elif (SmartBar_Dispenser.CommandType == "$SI"):

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
                SmartBar_Dispenser.CommandReturnMessage = ("$DS,SA,DO")

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
            SmartBar_Dispenser.CommandReturnMessage = ("$ER,DD,RO@"+drink_order)
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
        self.AlcoholValveDelay_Enabled = AlcoholValveDelayEnabled
        
        if (item_category == 0):

            for i in range(len(SmartBar_Dispenser.InventoryManager.Alcohols)): # scan through all alcohol in inventory

                if (item_type == SmartBar_Dispenser.InventoryManager.Alcohols[i].Type): # if the alcohol type matches continue

                        if (item_brand == SmartBar_Dispenser.InventoryManager.Alcohols[i].Brand): # if the brand also matches, the component is confirmed

                            if (SmartBar_Dispenser.InventoryManager.Alcohols[i].Active == 1): # if the container is currently in use valve info can be added to the list
                          
                                if ((SmartBar_Dispenser.InventoryManager.Alcohols[i].Volume-SBDispenser_MinimumContainerContents) < item_volume):
                                    SmartBar_Dispenser.CommandReturnMessage = ("$ER,DD,LV@A,"+str(item_type)+","+str(item_brand))
                                    SmartBar_Dispenser.CommandStatus = 0
                                    return
                      
                                ValveTime = float((float(item_volume/SmartBar_Dispenser.OzScalingFactor)) * SmartBar_Dispenser.AlcoholDispenseTimePerOz) # calculating valve open time
                                ValveNumber = SmartBar_Dispenser.InventoryManager.Alcohols[i].ValveNumber
                                ValveDelay = self.AlcoholValveDelay
                                SmartBar_Dispenser.PrintFilter.System((SBDispenser_MixType[int(item_category)]+' T:'+str(item_type)+' B:'+str(item_brand)+' Found - '+str(ValveTime)+'s to dispense '+str(item_volume)+'oz'),SmartBar_Dispenser.PrintFilter.Standard)

                                SmartBar_Dispenser.ValveManager.QueueValve(ValveNumber, ValveTime, ValveDelay,MixType_Alcohol)

                                if (self.AlcoholValveDelay_Enabled == 1):

                                    self.AlcoholValveDelay = self.AlcoholValveDelay + ValveTime

                                return ValveNumber
                                    
                                
            SmartBar_Dispenser.CommandStatus = 0
            SmartBar_Dispenser.CommandReturnMessage = ("$ER,DD,NF@A,"+str(item_type)+","+str(item_brand))
            SmartBar_Dispenser.PrintFilter.Error("Alcohol Drink Component Not Found - Type: "+str(item_type)+" , Brand: "+str(item_brand),SmartBar_Dispenser.PrintFilter.Title)

            


        if (item_category == 1):
                            
            for i in range(len(SmartBar_Dispenser.InventoryManager.Mixers)): # scan through all mixer in inventory

                    if (item_type == SmartBar_Dispenser.InventoryManager.Mixers[i].Type): # if the mixer type matches continue

                            if (item_brand == SmartBar_Dispenser.InventoryManager.Mixers[i].Brand): # if the brand also matches, the component is confirmed

                                if (SmartBar_Dispenser.InventoryManager.Mixers[i].Active == 1): # if the container is currently in use valve info can be added to the list

                                    if ((SmartBar_Dispenser.InventoryManager.Mixers[i].Volume-SBDispenser_MinimumContainerContents) < item_volume):
                                        SmartBar_Dispenser.CommandReturnMessage = ("$ER,DD,LV@M,"+str(item_type)+","+str(item_brand))
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
            SmartBar_Dispenser.CommandReturnMessage = ("$ER,DD,NF@M,"+str(item_type)+","+str(item_brand))
            SmartBar_Dispenser.PrintFilter.Error("Mixer Drink Component Not Found - Type: "+str(item_type)+" , Brand: "+str(item_brand),SmartBar_Dispenser.PrintFilter.Title)

            return SmartBar_Dispenser.FindDrinkComponent_Fail
                                                       


        
        

         

        
    
    def __init__(self): ## Sets up an instance of the SmartBar_Dispenser class by initializing gpio, creating instance of the SmartBar_Inventory class

        SmartBar_Dispenser.CommandStatus = 1
        SmartBar_Dispenser.CommandReturnMessage = "Inactive"
        SmartBar_Dispenser.PrintFilter = PrintLogFilter(1,1,1,1,2)
        SmartBar_Dispenser.PrintFilter.System("Starting Dispenser Initialization",SmartBar_Dispenser.PrintFilter.Title)
        self.GPIO_Initialize() # initialize GPIO
        SmartBar_Dispenser.ValveManager = SmartBar_ValveController(SmartBar_Dispenser.TotalDispensingValves) # initialize valve manager
        SmartBar_Dispenser.InventoryManager = SmartBar_Inventory() # initialize inventory manager
        SmartBar_Dispenser.PrintFilter.System("Dispenser Initialization Complete",SmartBar_Dispenser.PrintFilter.Title)
                                            


   
    def GPIO_Initialize(self): # Sets gpio mode, configures dispensing pins to be outputs
        
        SmartBar_Dispenser.PrintFilter.System("Power Control GPIO Initialize",SmartBar_Dispenser.PrintFilter.SubTitle)
        GPIO.setmode(GPIO.BCM) # set GPIO mode
        GPIO.setwarnings(False)                                        
        GPIO.setup(SmartBar_Dispenser.CoolCarbPower,GPIO.OUT) # set cooling/carbonation system control pin as an output
        GPIO.output(SmartBar_Dispenser.CoolCarbPower,GPIO.LOW) # set cooling/carbonation control pin low - cooling/carbonation system off - MUST REMAIN OFF UNTIL WATER PUMP HAS STARTED
        SmartBar_Dispenser.PrintFilter.System(("Cooling/Carbonation System GPIO Pin #"+str(SmartBar_Dispenser.CoolCarbPower)+" Initialized"),SmartBar_Dispenser.PrintFilter.Standard)
        GPIO.setup(SmartBar_Dispenser.WaterPumpPower,GPIO.OUT) # set water pump control pin as an output
        GPIO.output(SmartBar_Dispenser.WaterPumpPower,GPIO.LOW) # set water pump control pin low - water pump off                                    

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

        SmartBar_Dispenser.InventoryManager.UpdateInventoryFile()
##    def TimerThread(self):
##        
##        self.MaxDispenseTime = self.MaximumValveTime/1000 + .5
##
##        self.CurrentDispenseTime = time.time()
##
##        self.DispenseEndTime = self.CurrentDispenseTime + self.MaxDispenseTime
##
##        SmartBar_Dispenser.PrintFilter.System('Timer Module Started - Starting Time: '+str(self.CurrentDispenseTime)+' Ending Time: '+ str(self.DispenseEndTime), SmartBar_Dispenser.PrintFilter.SubTitle)
##
##        while(self.CurrentDispenseTime < self.DispenseEndTime):
##            
##            self.CurrentDispenseTime = time.time()
##
##        print('word!!!')
##            
##        SmartBar_Dispenser.CurrentlyDispensing = 0


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
            SmartBar_Dispenser.CommandReturnMessage = ("$ER,DD,RO@A,"+str(alcohol_number)+","+str(order_line))
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
            SmartBar_Dispenser.CommandReturnMessage = ("$ER,DD,RO@M,"+str(mixer_number)+","+str(order_line))
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
        self.ImportInventory(SBDispenser_CurrentInventoryFilePath)
        
    def ImportInventory(self,inventory_file_name):     
        try:
            InventoryFile = open(inventory_file_name,'r')
            self.StoredInventory = (InventoryFile.read()).split(SBDispenser_PacketSplittingCharacter)
        except IOError:
            SmartBar_Dispenser.PrintFilter.Error(("Failed to Open Inventory File : "+str(inventory_file_name)),SmartBar_Dispenser.PrintFilter.Title)
        
        self.CurrentLine = self.StoredInventory[0].split(SBDispenser_LineSplittingCharacter)
        self.NextLineToRead = 1
        SmartBar_Inventory.NumberOfAlcohols = int(self.CurrentLine[1])
        SmartBar_Inventory.NumberOfMixers = int(self.CurrentLine[2])
        SmartBar_Dispenser.PrintFilter.System,('\nFile: '+inventory_file_name+' opened - Total Mixers: '+str(SmartBar_Inventory.NumberOfMixers)+'  - Total Alcohols: '+str(SmartBar_Inventory.NumberOfAlcohols)+'\n',SmartBar_Dispenser.PrintFilter.Standard)
        self.ImportInventory_Alcohols()
        self.ImportInventory_Mixers()

        

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

    def UpdateInventoryFile(self):
    
        InventoryFile = open(SBDispenser_CurrentInventoryFilePath,'w')
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
            
        InventoryFile.write(self.UpdateInventoryString)                       
        InventoryFile.close()

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

        ReplaceInventoryFile = open(SBDispenser_CurrentInventoryFilePath,'w')
        ReplaceInventoryFile.write(inventory_string)
        self.UpdateInventoryFile()
        

        
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

    # Variables


    
    # Functions

    def __init__(self,total_valves): # set up gpio, set up variables based on the total amount of valves being used
        

        # Additional Variable Setup

        self.WaterValveNumber = WaterOutputValveNumber

        self.CarbonatedWaterValveNumber = CO2WaterOutputValveNumber

        self.TotalValves = total_valves # total number of valves in use

        self.CurrentValveState = [0] * total_valves # state (open/closed) of all of the valves, initialized to 0 (closed)

        self.QueuedValves = []

        # GPIO Setup

        self.SR_Data = ValveController_SR_Data # pin of serial output to shift register  
        self.SR_Clock = ValveController_SR_Clock # pin of clock to shift register
        self.SR_Store = ValveController_SR_Store # pin of store data to shift register   
        self.SR_ClockPause = ValveController_SR_ClockPause # time between high and low clock signal to shift register
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

            self.QueuedValves.append(ValveDispenseData(valve_number,ValveClosed, ValveHasNotOpened, valve_timing, valve_delay,mix_type))

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

        TestPause = 2
        
        self.ClearQueuedValves()
        (self.TotalValves) = 4
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



# Class: PrintFilter
# Description: Only prints statements if message type is enables
# Author: Brendan Short
# Last Modified: 2/13/2015

class PrintLogFilter():

    def __init__(self,system_info_enabled,debugging_info_enabled,warnings_enabled,error_messages_enabled,spacing_size):
        self.Spacing = spacing_size
        self.Title = 1
        self.SubTitle = 2
        self.Standard = 0
        self.MessageIndicators = ['$',"*","@","&"]
        self.SystemIndex = 0
        self.CommandIndex = 5
        self.DebugIndex = 1
        self.WarnIndex = 2
        self.ErrorIndex = 3
        self.WriteToAllLogs = 4
        self.MessagePrefix = ["Dispensing System : ","Debug : ","Warning : ", "Error : "]
        self.Space = " "
        self.NoSpace = ""
        self.MessageBarIndent = 5
        self.MessageBarSize = 75
        self.SpaceAfterLastPrinted = 0
        
        if (self.Spacing == 0):
            self.SpaceBeforeTitle = self.NoSpace
            self.SpaceAfterTitle = self.NoSpace
            self.SpaceBeforeSubTitle = self.NoSpace
            self.SpaceAfterSubTitle = self.NoSpace
            self.SpaceBeforeStandard = self.NoSpace
            self.SpaceAfterStandard = self.NoSpace
        if (self.Spacing == 1):
            self.SpaceBeforeTitle = '\n'
            self.SpaceAfterTitle = '\n'
            self.SpaceBeforeSubTitle = self.NoSpace
            self.SpaceAfterSubTitle = self.NoSpace
            self.SpaceBeforeStandard = self.NoSpace
            self.SpaceAfterStandard = self.NoSpace
        if (self.Spacing == 2):
            self.SpaceBeforeTitle = '\n'
            self.SpaceAfterTitle = '\n'
            self.SpaceBeforeSubTitle = '\n'
            self.SpaceAfterSubTitle = '\n'
            self.SpaceBeforeStandard = self.NoSpace
            self.SpaceAfterStandard = self.NoSpace
        if (self.Spacing == 3):
            self.SpaceBeforeTitle = '\n'
            self.SpaceAfterTitle = '\n'
            self.SpaceBeforeSubTitle = '\n'
            self.SpaceAfterSubTitle = '\n'
            self.SpaceBeforeStandard = '\n'
            self.SpaceAfterStandard = '\n'

        self.CurrentMessageToLog = 'Empty'



        self.LocalTime = time.localtime(time.time())
        self.SystemInfoEnabled = system_info_enabled # system information on/off
        self.DebuggingEnabled = debugging_info_enabled # debugging messages on/off
        self.WarningsEnabled = warnings_enabled # display warnings on/off
        self.ErrorMessagesEnabled = error_messages_enabled

        self.SystemDataLogEnabled = 1
        self.SystemTextLogEnabled = 1
        self.ErrorDataLogEnabled = 1
        self.ErrorTextLogEnabled = 1
        self.FullDataLogEnabled = 1
        self.FullTextLogEnabled = 1
        self.DrinkDataLogEnabled = 1
        self.DrinkTextLogEnabled = 1
        
        self.LogStartingCode = "$SD"
        self.StartDataLogger()

            
    def System(self, system_message, message_type):     
        if (self.SystemInfoEnabled == 1):
            if (message_type > 0):
                self.TitleMessage(self.SystemIndex,message_type,system_message)
            else:
                if (self.SpaceAfterLastPrinted == 1):
                    print(system_message+self.SpaceAfterStandard)
                    self.CurrentMessageToLog = (system_message+self.SpaceAfterStandard)
                else:
                    print(self.SpaceBeforeStandard+system_message+self.SpaceAfterStandard)
                    self.CurrentMessageToLog = (system_message+self.SpaceAfterStandard)

                if (self.SpaceAfterStandard == '\n'):
                    self.SpaceAfterLastPrinted = 1
                else:
                    self.SpaceAfterLastPrinted = 0
        self.WriteToSpecificTextLogFiles(self.SystemIndex)
                    
    def Debug(self, debug_message,message_type):
        
        if (self.DebuggingEnabled == 1):
            
            if (message_type > 0):
                self.TitleMessage(self.DebugIndex, message_type,debug_message)
            else:
                
                if (self.SpaceAfterLastPrinted == 1):
                    print(self.MessageIndicators[self.DebugIndex]+self.MessagePrefix[self.DebugIndex]+debug_message+self.SpaceAfterStandard)
                else:
                    print(self.SpaceBeforeStandard+self.MessageIndicators[self.DebugIndex]+self.MessagePrefix[self.DebugIndex]+debug_message+self.SpaceAfterStandard)

                if (self.SpaceAfterStandard == '\n'):
                    self.SpaceAfterLastPrinted = 1
                else:
                    self.SpaceAfterLastPrinted = 0

                    
    def Warn(self, warning_message, message_type):          
        if (self.WarningsEnabled == 1):
            if (message_type > 0):
                self.TitleMessage(self.WarnIndex, message_type,warning_message)
            else:
                if (self.SpaceAfterLastPrinted == 1):
                    print(self.MessageIndicators[self.WarnIndex]+self.MessagePrefix[self.WarnIndex]+warning_message+self.SpaceAfterStandard)
                    self.CurrentMessageToLog = (self.MessageIndicators[self.WarnIndex]+self.MessagePrefix[self.WarnIndex]+warning_message+self.SpaceAfterStandard)
                else:
                    print(self.SpaceBeforeStandard+self.MessageIndicators[self.WarnIndex]+self.MessagePrefix[self.WarnIndex]+warning_message+self.SpaceAfterStandard)
                    self.CurrentMessageToLog = (self.SpaceBeforeStandard+self.MessageIndicators[self.WarnIndex]+self.MessagePrefix[self.WarnIndex]+warning_message+self.SpaceAfterStandard)
                    
                if (self.SpaceAfterStandard == '\n'):
                    self.SpaceAfterLastPrinted = 1
                else:
                    self.SpaceAfterLastPrinted = 0
                    
                    
    def Error(self, error_message, message_type):          
        if (self.ErrorMessagesEnabled == 1):
            if (message_type > 0):
                self.TitleMessage(self.ErrorIndex,message_type,error_message)
            else:
                if (self.SpaceAfterLastPrinted == 1):
                    print(self.MessageIndicators[self.ErrorIndex]+self.MessagePrefix[self.ErrorIndex]+error_message+self.SpaceAfterStandard)
                    self.CurrentMessageToLog = (self.MessageIndicators[self.ErrorIndex]+self.MessagePrefix[self.ErrorIndex]+error_message+self.SpaceAfterStandard)
                else:
                    print(self.SpaceBeforeStandard+self.MessageIndicators[self.ErrorIndex]+self.MessagePrefix[self.ErrorIndex]+error_message+self.SpaceAfterStandard)            
                    self.CurrentMessageToLog = (self.SpaceBeforeStandard+self.MessageIndicators[self.ErrorIndex]+self.MessagePrefix[self.ErrorIndex]+error_message+self.SpaceAfterStandard)
                    
                if (self.SpaceAfterStandard == '\n'):
                    self.SpaceAfterLastPrinted = 1
                else:
                    self.SpaceAfterLastPrinted = 0
        self.WriteToSpecificTextLogFiles(self.ErrorIndex)
        
    def TitleMessage(self, filter_type, message_type,message_content):


        MessageLength = len(message_content) + len(self.MessagePrefix[filter_type]) + self.MessageBarIndent + 3*len(self.Space)                                                
        MessageBar = self.NoSpace.join([self.MessageIndicators[filter_type]] * self.MessageBarSize )  # state (open/closed) of all of the valves, initialized to 0 (closed)
        if (len(message_content) > (self.MessageBarSize - len(self.Space.join((self.NoSpace.join([self.MessageIndicators[filter_type]] * self.MessageBarIndent),self.MessagePrefix[filter_type]))))):
            Message = self.Space.join((self.NoSpace.join([self.MessageIndicators[filter_type]] * self.MessageBarIndent),
                                       self.MessagePrefix[filter_type],MessageBar[(int(MessageLength)-1-len(message_content)):],'\n',message_content))
        else:
            Message = self.Space.join((self.NoSpace.join([self.MessageIndicators[filter_type]] * self.MessageBarIndent),self.MessagePrefix[filter_type],message_content,MessageBar[int(MessageLength):]))
        if (message_type == self.Title):

            CurrentDate = time.time()
            TimeLogBar = (self.NoSpace.join([self.MessageIndicators[filter_type]] * self.MessageBarIndent)+' '+str(self.LocalTime[2])+
                          '/'+str(self.LocalTime[1])+'/'+str(self.LocalTime[0])+' @ '+str(self.LocalTime[3])+':'+
                          str(self.LocalTime[4])+':'+str(self.LocalTime[5])+' ')

            TimeLogBarLength = len(TimeLogBar)
            TimeLogBar = TimeLogBar+MessageBar[TimeLogBarLength:]
            if (self.SpaceAfterLastPrinted == 1):
                print(MessageBar+'\n'+TimeLogBar)
                self.CurrentMessageToLog = MessageBar+'\n'+TimeLogBar
            else:
                print(self.SpaceBeforeTitle+MessageBar+'\n'+TimeLogBar)
                self.CurrentMessageToLog = (self.SpaceBeforeTitle+MessageBar+'\n'+TimeLogBar)
                                    
            print(Message)
            print(MessageBar +self.SpaceAfterTitle)
            self.CurrentMessageToLog = self.CurrentMessageToLog+'\n'+str(Message)+'\n'+(MessageBar +self.SpaceAfterTitle)
            if (self.SpaceAfterTitle == '\n'):
                self.SpaceAfterLastPrinted = 1
            else:
                self.SpaceAfterLastPrinted = 0   

        else:

            if (self.SpaceAfterLastPrinted == 1):
                print(Message+self.SpaceAfterSubTitle)
                self.CurrentMessageToLog = (Message+self.SpaceAfterSubTitle)
            else:
                print(self.SpaceBeforeSubTitle+Message +self.SpaceAfterSubTitle)
                self.CurrentMessageToLog = (self.SpaceBeforeSubTitle+Message +self.SpaceAfterSubTitle)

            if (self.SpaceAfterSubTitle == '\n'):
                self.SpaceAfterLastPrinted = 1
            else: 
                self.SpaceAfterLastPrinted = 0


    def StartDataLogger(self):

        StartUpTimeCode = SBDispenser_LineSplittingCharacter.join(([str(self.LocalTime[0]),str(self.LocalTime[1]),str(self.LocalTime[2]),
                                                                           str(self.LocalTime[3]),str(self.LocalTime[4]),str(self.LocalTime[5])]))        
        LogStartDateCode = SBDispenser_PacketSplittingCharacter.join([self.LogStartingCode,(StartUpTimeCode+'\n')])
        StartUpDateString = ('Start Up #  - '+str(self.LocalTime[2])+'/'+str(self.LocalTime[1])+'/'+str(self.LocalTime[0])+' at '+
                                                                           str(self.LocalTime[3])+':'+str(self.LocalTime[4])+':'+str(self.LocalTime[5]))
        self.System(StartUpDateString,self.Title)
        self.OpenLogFiles()
   #     self.WriteToAllTextLogFiles(StartUpDateString)
        self.WriteToAllDataLogFiles(LogStartDateCode)
        self.CloseLogFiles()                    



    def OpenLogFiles(self):

        if (self.SystemDataLogEnabled == 1):
            self.SystemDataFile = open(SBDispenser_SystemDataLogFilePath,'a')
        if (self.SystemTextLogEnabled == 1):
            self.SystemTextFile = open(SBDispenser_SystemTextLogFilePath,'a')
        if (self.ErrorDataLogEnabled == 1):
            self.ErrorDataFile = open(SBDispenser_ErrorDataLogFilePath,'a')
        if (self.ErrorTextLogEnabled == 1):
            self.ErrorTextFile = open(SBDispenser_ErrorTextLogFilePath,'a')
        if (self.FullDataLogEnabled == 1):
            self.FullDataFile = open(SBDispenser_FullDataLogFilePath,'a')
        if (self.FullTextLogEnabled == 1):
            self.FullTextFile = open(SBDispenser_FullTextLogFilePath,'a')
        if (self.DrinkDataLogEnabled == 1):
            self.DrinkDataFile = open(SBDispenser_DrinkDataLogFilePath,'a')
        if (self.DrinkTextLogEnabled == 1):
            self.DrinkTextFile = open(SBDispenser_DrinkTextLogFilePath,'a')  

    def CloseLogFiles(self):

        if (self.SystemDataLogEnabled == 1):
            self.SystemDataFile.close()
        if (self.SystemTextLogEnabled == 1):
            self.SystemTextFile.close()
        if (self.ErrorDataLogEnabled == 1):
            self.ErrorDataFile.close()
        if (self.ErrorTextLogEnabled == 1):
            self.ErrorTextFile.close()
        if (self.FullDataLogEnabled == 1):
            self.FullDataFile.close()
        if (self.FullTextLogEnabled == 1):
            self.FullTextFile.close()
        if (self.DrinkDataLogEnabled == 1):
            self.DrinkDataFile.close()
        if (self.DrinkTextLogEnabled == 1):
            self.DrinkTextFile.close() 

    def WriteToAllTextLogFiles(self,message):

        if (self.SystemTextLogEnabled == 1):
            self.SystemDataFile.write(message)
        if (self.ErrorTextLogEnabled == 1):
            self.ErrorTextFile.write(message)
        if (self.FullTextLogEnabled == 1):
            self.FullTextFile.write(message)
        if (self.DrinkTextLogEnabled == 1):
            self.DrinkTextFile.write(message)

    def WriteToSpecificTextLogFiles(self,message_type):
        self.OpenLogFiles() 
        if (message_type == self.ErrorIndex):
            if (self.ErrorTextLogEnabled == 1):
                self.ErrorTextFile.write((self.CurrentMessageToLog+'\n'))
        if (self.FullTextLogEnabled == 1):
            self.FullTextFile.write((self.CurrentMessageToLog+'\n'))
        self.CloseLogFiles() 

    def WriteToSystemTextLogFile(self):
        self.OpenLogFiles() 
        if (self.SystemTextLogEnabled == 1):
            self.SystemTextFile.write((self.CurrentMessageToLog+'\n'))
        self.CloseLogFiles()
        
    def WriteToAllDataLogFiles(self,message):

        if (self.SystemDataLogEnabled == 1):
            self.SystemDataFile.write(message)
        if (self.ErrorDataLogEnabled == 1):
            self.ErrorDataFile.write(message)
        if (self.FullDataLogEnabled == 1):
            self.FullDataFile.write(message)
        if (self.DrinkDataLogEnabled == 1):
            self.DrinkDataFile.write(message)        
  #  def DataLogger(self,message)

##class DataLogger():
##
##    def __init__(self,inventory_log_enabled,drink_log_enabled):
##
##        self.InventoryLoggingEnabled = inventory_log_enabled
##        self.DrinkLoggingEnabled = drink_log_enabled
##
##    def PrintDateBar()

        
##def main():
##
##                                            
##    Dispenser = SmartBar_Dispenser()
##
## #   Dispenser.ValveManager.TestAllValves()
##    
## #
##
## #   Dispenser.ReceiveCommand("$DO,1,0@W,1,1.0") 
##
## #   Dispenser.ReceiveCommand("$DO,0,1@GA,0,0,1.0")
##
##    Dispenser.ReceiveCommand("$DO,2,2@WH,1,0.1@GN,1,0.1@GT,1,0,0.1@TO,1,0,0.1")
## #   Dispenser.GPIO_Free()
##    
##if __name__ == '__main__':
##    main()

        

    
            



