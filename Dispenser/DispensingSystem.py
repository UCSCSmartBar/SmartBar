# !/home/pi/Desktop/SmartBar
# FileName: DispensingPrototype.py
# Description: A test driver for the dispensing system
# Author: Brendan Short
# Date: 2/13/2015
import sys
sys.path.append('/SmartBar/Dispenser/')
sys.path.append('/SmartBar/System/')
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

    InventoryFilePath = "/SmartBar/Dispenser/UCSC_SmartBar_Inventory.sb" # file where inventory data is stored

    OzScalingFactor = 1 # scale up ounces to transfer data more compactly - ie: 1.5oz = 15

    AlcoholDispenseTimePerOz = 4 # time in seconds to dispense one fluid ounce of alcohol

    MixerDispenseTimePerOz = 1.5 # time in seconds to dispense one fluid ounce of mixer

    WaterValveNumber = 18

    CarbonatedWaterValveNumber = 19

    TimerInterval = .001 # interval between valve timing check routines (while dispensing)

    CurrentlyDispensing = 1 # flag indicating if dispensing

    LineSplittingCharacter = ","

    PacketSplittingCharacter = "@"


    # Functions

    def ReceiveCommand(self, incoming_command):

        self.CommandPacket = incoming_command # store incoming command packet

        self.CommandType = incoming_command.split(SmartBar_Dispenser.PacketSplittingCharacter)[0].split(SmartBar_Dispenser.LineSplittingCharacter)[0] # get the incoming command

        SmartBar_Dispenser.PrintFilter.System(("Received Command : "+self.CommandType+" Input String : "+self.CommandPacket),SmartBar_Dispenser.PrintFilter.SubTitle)
        
        if (self.CommandType == "$DO"):

            self.CommandStatus = self.DispenseDrinkOrder(self.CommandPacket)







    def DispenseDrinkOrder(self,incoming_drink_order): # recieve order from tablet, then analyze and dispense - main function call of this module

        self.OrderStatus = self.ReceiveDrinkOrder(incoming_drink_order)

        if (self.OrderStatus == 1):

            self.OrderStatus = self.ProcessDrinkOrder()
##
      #      if (self.CurrentDrink.Status == 2):
##

            SmartBar_Dispenser.ValveManager.OpenQueuedValves()
        


          
        return self.OrderStatus





    def ReceiveDrinkOrder(self, drink_order):

        RecieveOrderStatus = 0 # 1 if success -1 if failure, to be returned to DispenseDrinkOrder 


            #PacketFormat:     $(Drink Order).(Number of Alcohols).(Number of Mixers)@
            #                  (Alcohol Type).(Alcohol Brand).(Dispense Volume)@
            #                                            :
            #                  (Mixer Type).(Mixer Brand).(Carbonation).(Dispense Volume)@
            #                                            :


        SmartBar_Dispenser.PrintFilter.Debug("Start Receive Drink Order",SmartBar_Dispenser.PrintFilter.Title)

        SmartBar_Dispenser.PrintFilter.Debug("Input String: "+drink_order+"\n",SmartBar_Dispenser.PrintFilter.Standard)
            
            
        while(RecieveOrderStatus == 0):

            #try:

                self.OrderPacket = drink_order.split(SmartBar_Dispenser.PacketSplittingCharacter) # break up the order into rows to be processed
                
                self.GeneralDrinkInfo = self.OrderPacket[0].split(SmartBar_Dispenser.LineSplittingCharacter) # get the general drink info - $DO.X.Y where $DO is the drink order command, X is the number of alcohol components, and Y is the number of conentrate components

                self.NumOfAlcoholComponents = int(self.GeneralDrinkInfo[1]) # number of alcohol components

                self.NumOfMixerComponents = int(self.GeneralDrinkInfo[2]) # number of Mixer components

                SmartBar_Dispenser.PrintFilter.Debug("Number of Alcohol Components : "+str(self.NumOfAlcoholComponents)+" Number of Mixer Components : "+str(self.NumOfMixerComponents)+" Packet Length : "+str(len(self.OrderPacket)),SmartBar_Dispenser.PrintFilter.Standard)
    
       #         if ((self.NumOfAlcoholComponents + self.NumOfMixerComponents) == (len(self.OrderPacket) - 1)): # if the packet is the right size continue

      
                self.CurrentDrink = SmartBar_DrinkOrder(self.NumOfAlcoholComponents,self.NumOfMixerComponents) # create a new drink order

                for i in range(self.NumOfAlcoholComponents): # store all alcohol drink components

                    self.CurrentDrink.StoreAlcoholComponent(i,self.OrderPacket[i+1]) # store individual alcohol drink component

                for i in range(self.NumOfMixerComponents): # store all Mixer drink components

                    self.CurrentDrink.StoreMixerComponent(i,self.OrderPacket[i+1+self.NumOfAlcoholComponents]) # store individual Mixer drink component

                print('\n<<<<< Start: Receive Drink Order Debug: Storing Drink Order>>>>>> \n'+'\nInput String: '+drink_order+'\n')

                self.CurrentDrink.PrintDrinkOrderData() # print the order currently stored

                print('\n<<<<< End: Receive Drink Order Debug: Storing Drink Order>>>>> \n')

                RecieveOrderStatus = 1 # recieve drink order successful    

            #except:

                SmartBar_Dispenser.PrintFilter.Error('Failure: Receive Drink Order',SmartBar_Dispenser.PrintFilter.Title)

               # RecieveOrderStatus = -1 # recieve drink order failed

        return RecieveOrderStatus



 

    def ProcessDrinkOrder(self):

        self.AlcoholValveDelay = 0
        
        for i in range(self.CurrentDrink.NumberOfAlcoholComponents): # scan through all alcohol components of current drink
           
            Drink_Category = 0

            AlcoholType = self.CurrentDrink.Alcohols[i].Type

            AlcoholBrand = self.CurrentDrink.Alcohols[i].Brand

            DispenseVolume = self.CurrentDrink.Alcohols[i].Volume

            self.ProcessStatus = self.SetupValve(Drink_Category,AlcoholType,AlcoholBrand,0,DispenseVolume)

            if (self.ProcessStatus != 1):

                return -1

        self.MixerValveDelay = 0

        for i in range(self.CurrentDrink.NumberOfMixerComponents): # scan through all alcohol components of current drink
           
            Drink_Category = 1

            MixerType = self.CurrentDrink.Mixers[i].Type

            MixerBrand = self.CurrentDrink.Mixers[i].Brand

            MixerCarbonation = self.CurrentDrink.Mixers[i].Carbonated

            DispenseVolume = self.CurrentDrink.Mixers[i].Volume

            self.ProcessStatus = self.SetupValve(Drink_Category,MixerType,MixerBrand,MixerCarbonation,DispenseVolume)

            if (self.ProcessStatus != 1):

                return -1

        
        return 2
    

    def SetupValve(self,item_category,item_type,item_brand,item_carbonation,item_volume):


        MixType_Alcohol = 0

        MixType_H2OMixer = 1

        MixType_CO2Mixer = 2
        
        self.AlcoholValveDelay_Enabled = AlcoholValveDelayEnabled
        
        if (item_category == 0):

            for i in range(len(SmartBar_Dispenser.InventoryManager.Alcohols)): # scan through all alcohol in inventory

                if (item_type == SmartBar_Dispenser.InventoryManager.Alcohols[i].Type): # if the alcohol type matches continue

                        if (item_brand == SmartBar_Dispenser.InventoryManager.Alcohols[i].Brand): # if the brand also matches, the component is confirmed

                            if (SmartBar_Dispenser.InventoryManager.Alcohols[i].Active == 1): # if the container is currently in use valve info can be added to the list
                            
                                ValveTime = float((float(item_volume/SmartBar_Dispenser.OzScalingFactor)) * SmartBar_Dispenser.AlcoholDispenseTimePerOz) # calculating valve open time

                                ValveNumber = SmartBar_Dispenser.InventoryManager.Alcohols[i].ValveNumber
            
                                ValveDelay = self.AlcoholValveDelay

                                Status = SmartBar_Dispenser.ValveManager.QueueValve(ValveNumber, ValveTime, ValveDelay,MixType_Alcohol)

                                if (self.AlcoholValveDelay_Enabled == 1):

                                    self.AlcoholValveDelay = self.AlcoholValveDelay + ValveTime
                                    
                                return 1


            SmartBar_Dispenser.PrintFilter.Error("Drink Component Not Found - Type: "+str(item_type)+" , Brand: "+str(item_brand),SmartBar_Dispenser.PrintFilter.Title)

            return SmartBar_Dispenser.FindDrinkComponent_Fail


        if (item_category == 1):
                            
            for i in range(len(SmartBar_Dispenser.InventoryManager.Mixers)): # scan through all mixer in inventory

                    if (item_type == SmartBar_Dispenser.InventoryManager.Mixers[i].Type): # if the mixer type matches continue

                            if (item_brand == SmartBar_Dispenser.InventoryManager.Mixers[i].Brand): # if the brand also matches, the component is confirmed

                                if (SmartBar_Dispenser.InventoryManager.Mixers[i].Active == 1): # if the container is currently in use valve info can be added to the list

                                    ValveTime = float((float(item_volume/SmartBar_Dispenser.OzScalingFactor) * SmartBar_Dispenser.MixerDispenseTimePerOz)) # calculating valve open time

                                    ValveNumber = SmartBar_Dispenser.InventoryManager.Mixers[i].ValveNumber

                                    ValveDelay = self.MixerValveDelay

                                    self.MixerValveDelay = self.MixerValveDelay + ValveTime

                                    if (item_carbonation == 0):

                                        MixerType = MixType_H2OMixer

                                    if (item_carbonation == 1):
                                        
                                        MixerType = MixType_CO2Mixer

                                    Status = SmartBar_Dispenser.ValveManager.QueueValve(ValveNumber,ValveTime,ValveDelay,MixerType)
                                        

                                    return Status


            SmartBar_Dispenser.PrintFilter.Error("Drink Component Not Found",SmartBar_Dispenser.PrintFilter.Title)

            return SmartBar_Dispenser.FindDrinkComponent_Fail
                                                       


        
        

         

        
    
    def __init__(self): ## Sets up an instance of the SmartBar_Dispenser class by initializing gpio, creating instance of the SmartBar_Inventory class

        SmartBar_Dispenser.PrintFilter = Print_Filter(1,1,1,1)

        SmartBar_Dispenser.PrintFilter.System("Starting Dispenser Initialization",SmartBar_Dispenser.PrintFilter.Title)

        self.GPIO_Initialize() # initialize GPIO

        SmartBar_Dispenser.ValveManager = SmartBar_ValveController(SmartBar_Dispenser.TotalDispensingValves) # initialize valve manager

        SmartBar_Dispenser.InventoryManager = SmartBar_Inventory(SmartBar_Dispenser.InventoryFilePath) # initialize inventory manager

        SmartBar_Dispenser.PrintFilter.System("Dispenser Initialization Complete",SmartBar_Dispenser.PrintFilter.Title)
                                              


   
    def GPIO_Initialize(self): # Sets gpio mode, configures dispensing pins to be outputs

        GPIO.setmode(GPIO.BCM) # set GPIO mode

        GPIO.setup(SmartBar_Dispenser.CoolCarbPower,GPIO.OUT) # set cooling/carbonation system control pin as an output

        GPIO.output(SmartBar_Dispenser.CoolCarbPower,GPIO.LOW) # set cooling/carbonation control pin low - cooling/carbonation system off - MUST REMAIN OFF UNTIL WATER PUMP HAS STARTED

        GPIO.setup(SmartBar_Dispenser.WaterPumpPower,GPIO.OUT) # set water pump control pin as an output

        GPIO.output(SmartBar_Dispenser.WaterPumpPower,GPIO.LOW) # set water pump control pin low - water pump off

        SmartBar_Dispenser.PrintFilter.System("GPIO Initialize",SmartBar_Dispenser.PrintFilter.SubTitle)
                                              

    def GPIO_Free(self): # releases all GPIO pins

        GPIO.cleanup() # unexport all GPIO
        
        print ('GPIO Freed')


            
    def TimerThread(self):
        
        self.MaxDispenseTime = self.MaximumValveTime/1000 + .5

        self.CurrentDispenseTime = time.time()

        self.DispenseEndTime = self.CurrentDispenseTime + self.MaxDispenseTime

        SmartBar_Dispenser.PrintFilter.Debug('Timer Module Started - Starting Time: '+str(self.CurrentDispenseTime)+' Ending Time: '+ str(self.DispenseEndTime), SmartBar_Dispenser.PrintFilter.SubTitle)

        while(self.CurrentDispenseTime < self.DispenseEndTime):
            
            self.CurrentDispenseTime = time.time()

        print('word!!!')
            
        SmartBar_Dispenser.CurrentlyDispensing = 0


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

        try:                                   

            self.Status = 0 # drink has not yet been poured successfully

            self.NumberOfAlcoholComponents = num_alcohol_components # store number of alcoholic components 

            self.NumberOfMixerComponents = num_mixer_components # store number of mixer components 

            self.Alcohols = [] # set up list of alcohols to be filled

            self.Mixers = [] # set up list of Mixers to be filled
                                                        
        except:

            return -1

    
    def StoreAlcoholComponent(self, alcohol_number, order_line): # store alcohol component information

        try: # attempt to store current alcohol data the order

            ComponentInfo = order_line.split(SmartBar_Dispenser.LineSplittingCharacter) # break up the string (Alcohol Type).(Alcohol Brand).(Dispense Volume)

            AlcoholType = ComponentInfo[0] # get alcohol type

            AlcoholBrand = int(ComponentInfo[1]) # get alcohol brand

            AlcoholDispenseVolume = float(ComponentInfo[2]) # get alcohol dispense volume

            self.Alcohols.append(SmartBar_DrinkComponent_Alcohol(AlcoholType,AlcoholBrand,AlcoholDispenseVolume)) # store alcohol component data

        except:

            print('StoreAlcoholComponentFail')


    def StoreMixerComponent(self, mixer_number, order_line): # store mixer component information

        ComponentInfo = order_line.split(SmartBar_Dispenser.LineSplittingCharacter) # break up the string (Mixer Type).(Mixer Brand).(Carbonated).(Dispense Volume)

        MixerType = ComponentInfo[0] # get Mixer type

        MixerBrand = int(ComponentInfo[1]) # get Mixer brand

        MixerCarbonation = int(ComponentInfo[2]) # get carbonation choice

        MixerDispenseVolume = float(ComponentInfo[3]) # get mixer  dispense volume

        self.Mixers.append(SmartBar_DrinkComponent_Mixer(MixerType,MixerBrand,MixerCarbonation,MixerDispenseVolume)) # store Mixer component data


        
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


        
#Class: SmartBar_Inventory
#Description: A class which stores and manages all inventory data
#Author: Brendan Short
#Last Modified: 2/12/2015

class SmartBar_Inventory():

    def __init__(self,inventory_file):

        SmartBar_Inventory.Alcohols = [] # set up list of alcohols to be filled

        SmartBar_Inventory.Mixers = [] # set up list of Mixers to be filled

        SmartBar_Dispenser.PrintFilter.System("Importing Inventory",SmartBar_Dispenser.PrintFilter.SubTitle)
        self.ImportInventory(inventory_file)
        




    def ImportInventory(self,inventory_file_name):

        self.ImportInventoryDebug = 1

        

        try:
            
            InventoryFile = open(inventory_file_name,'r')

            self.StoredInventory = InventoryFile.readlines()
            

        except IOError:

            print("Open/Read Inventory Failed")

        
        CurrentLine = self.StoredInventory[0].split(".")

        self.NextLineToRead = 1

        SmartBar_Inventory.NumberOfAlcohols = int(CurrentLine[0])

        SmartBar_Inventory.NumberOfMixers = int(CurrentLine[1])

        if (self.ImportInventoryDebug == 1):

            SmartBar_Dispenser.PrintFilter.System('\nFile: '+inventory_file_name+' opened - Total Mixers: '+str(SmartBar_Inventory.NumberOfMixers)+'  - Total Alcohols: '+str(SmartBar_Inventory.NumberOfAlcohols)+'\n',SmartBar_Dispenser.PrintFilter.Standard)

        self.ImportInventory_Alcohols()

        self.ImportInventory_Mixers()

        

    def ImportInventory_Mixers(self):

        for i in range(SmartBar_Inventory.NumberOfMixers):

            CurrentLine = self.StoredInventory[self.NextLineToRead].split(".")
                        
            self.NextLineToRead = self.NextLineToRead + 1

            MixerActive = int(CurrentLine[0])

            MixerValveNumber = int(CurrentLine[1])

            MixerName = CurrentLine[2]

            MixerType = CurrentLine[3]

            MixerBrand = int(CurrentLine[4])

            MixerCarbonation = int(CurrentLine[5])

            MixerVolume = int(CurrentLine[6])
        
            SmartBar_Inventory.Mixers.append(SmartBar_Inventory_Mixer(MixerActive,MixerValveNumber,MixerName,MixerType,
                                                                                  MixerBrand,MixerCarbonation,MixerVolume)) # store Mixer inventory data
        
            SmartBar_Dispenser.PrintFilter.System('\nMixer'+str(i+1)+' - Active: '+str(SmartBar_Inventory.Mixers[i].Active)+' ;  Valve Number: '
                  +str(SmartBar_Inventory.Mixers[i].ValveNumber)+' ;  Name: '+SmartBar_Inventory.Mixers[i].Name+' ;  Type(#): '+str(SmartBar_Inventory.Mixers[i].Type)+
                  ' ;  Brand(#): '+str(SmartBar_Inventory.Mixers[i].Brand)+' ;  Carbonated: '+str(SmartBar_Inventory.Mixers[i].Carbonated)
                  +' ;  Volume: '+str(SmartBar_Inventory.Mixers[i].Volume), SmartBar_Dispenser.PrintFilter.Standard)

            

    def ImportInventory_Alcohols(self):

        for i in range(SmartBar_Inventory.NumberOfAlcohols):

            CurrentLine = self.StoredInventory[self.NextLineToRead].split(".")
                        
            self.NextLineToRead = self.NextLineToRead + 1

            AlcoholActive = int(CurrentLine[0])

            AlcoholValveNumber = int(CurrentLine[1])

            AlcoholName = CurrentLine[2]

            AlcoholType = CurrentLine[3]

            AlcoholBrand = int(CurrentLine[4])

            AlcoholVolume = int(CurrentLine[5])
        
            SmartBar_Inventory.Alcohols.append(SmartBar_Inventory_Alcohol(AlcoholActive,AlcoholValveNumber,AlcoholName,AlcoholType,AlcoholBrand,AlcoholVolume)) # store Alcohol inventory data
        
            SmartBar_Dispenser.PrintFilter.System('\nAlcohol'+str(i+1)+' - Active: '+str(SmartBar_Inventory.Alcohols[i].Active)+' ;  Valve Number: '
                  +str(SmartBar_Inventory.Alcohols[i].ValveNumber)+' ;  Name: '+SmartBar_Inventory.Alcohols[i].Name+' ;  Type(#): '+str(SmartBar_Inventory.Alcohols[i].Type)+
                  ' ;  Brand(#): '+str(SmartBar_Inventory.Alcohols[i].Brand)+' ;  Volume: '+str(SmartBar_Inventory.Alcohols[i].Volume),SmartBar_Dispenser.PrintFilter.Standard)




#Class: SmartBar_Inventory_Mixer
#Description: A data type to store the properties of individual Mixers in the SmartBar inventory
#Author: Brendan Short
#Last Modified: 2/12/2015

class SmartBar_Inventory_Mixer(): # class to store individual Mixer properties

    def __init__(self,Mixer_active,valve_number,Mixer_name,Mixer_type,Mixer_brand,Mixer_volume,Mixer_carbonation): # import properties

        self.Active = Mixer_active # flag indicating if Mixer is available to be mixed - in stock

        self.ValveNumber = valve_number # number of the valve connected to the corresponding dispensing pump
        
        self.Name = Mixer_name # Mixer name - ie: "Canada Dry Ginger Ale"

        self.Type = Mixer_type # Mixer type (#) - ie: soda = 1,juice = 2, energy drink = 3
        
        self.Brand = Mixer_brand # Mixer brand (#)  - ie: coke = 1, 7up = 2, fanta = 3
    
        self.Carbonated = Mixer_carbonation # carbonated water normally mixed in or not - ie: sodas = 1, juices = 0

        self.Volume = Mixer_volume # amount of Mixer remaining

   
            

#Class: SmartBar_Inventory_Alcohol
#Description: A data type to store the properties of individual alcohols in the SmartBar inventory
#Author: Brendan Short
#Last Modified: 2/12/2015
        
class SmartBar_Inventory_Alcohol(): 
    
    def __init__(self,alcohol_active,valve_number,alcohol_name,alcohol_type,alcohol_brand,alcohol_volume): # import properties

        self.Active = alcohol_active # flag indicating if Mixer is available to be mixed - in stock

        self.ValveNumber = valve_number # number of the valve connected to the corresponding dispensing pump

        self.Name = alcohol_name # alcohol name - ie: "Smirnoff Vodka"

        self.Type = alcohol_type # alcohol type (#) - ie: tequila = 1, rum = 2, vodka
        
        self.Brand = alcohol_brand # alcohol brand (#) - ie: grey goose = 1, smirnoff = 2
    
        self.Volume = alcohol_volume # amount of alcohol remaining



#Class: SmartBar_Valve Controller
#Description: A class which stores all of the current valve states (open/close) and controls the shift registers to open and close the valves
#Author: Brendan Short
#Last Modified: 2/12/2015
        
class SmartBar_ValveController(): 

    # Variables


    
    # Functions

    def __init__(self,total_valves): # set up gpio, set up variables based on the total amount of valves being used
        

        # Additional Variable Setup

        self.WaterValveNumber = 18

        self.CarbonatedWaterValveNumber = 19

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

        SmartBar_Dispenser.PrintFilter.Debug('Valve Monitor Started - Starting Time: '+str(self.DispenseStartTime)+' Ending Time: '+ str(self.DispenseEndTime), SmartBar_Dispenser.PrintFilter.Standard)

        self.CurrentDispenseTime = time.time()

        while(self.CurrentDispenseTime < self.DispenseEndTime):

            self.CurrentDispenseTime = time.time()

            self.ValveMonitor()
        
        self.UpdateShiftRegisters()


    def ValveMonitor(self):

        Changes = 0

        for i in range(len(self.QueuedValves)):

            if (  (self.CurrentDispenseTime >= (self.QueuedValves[i].Delay + self.QueuedValves[i].OpenTime + self.DispenseStartTime)) & (self.QueuedValves[i].CurrentlyOpen == 1)):

                SmartBar_Dispenser.PrintFilter.Debug("valve # "+str(self.QueuedValves[i].ValveNumber)+" closed  @ dispense time: "+str(self.CurrentDispenseTime-self.DispenseStartTime)+" seconds", SmartBar_Dispenser.PrintFilter.Standard)

                self.QueuedValves[i].CurrentlyOpen = 0

                self.CurrentValveState[self.QueuedValves[i].ValveNumber] = 0

                if (self.QueuedValves[i].Mix == 1):
                    
                    SmartBar_Dispenser.PrintFilter.Debug("valve # "+str(self.WaterValveNumber)+" closed  @ dispense time: "+str(self.CurrentDispenseTime-self.DispenseStartTime)+" seconds", SmartBar_Dispenser.PrintFilter.Standard)

                    self.CurrentValveState[self.WaterValveNumber] = 0

                if (self.QueuedValves[i].Mix == 2):

                    SmartBar_Dispenser.PrintFilter.Debug("valve # "+str(self.CarbonatedWaterValveNumber)+" closed  @ dispense time: "+str(self.CurrentDispenseTime-self.DispenseStartTime)+" seconds", SmartBar_Dispenser.PrintFilter.Standard)

                    self.CurrentValveState[self.CarbonatedWaterValveNumber] = 0

                Changes = 1
                

            if ((self.CurrentDispenseTime >= (self.QueuedValves[i].Delay + self.DispenseStartTime)) & (self.QueuedValves[i].HasBeenOpened != 1)):

                SmartBar_Dispenser.PrintFilter.Debug("valve # "+str(self.QueuedValves[i].ValveNumber)+" opened  @ dispense time: "+str(self.CurrentDispenseTime-self.DispenseStartTime)+" seconds", SmartBar_Dispenser.PrintFilter.Standard)

                self.QueuedValves[i].HasBeenOpened = 1

                self.QueuedValves[i].CurrentlyOpen = 1

                self.CurrentValveState[self.QueuedValves[i].ValveNumber] = 1

                if (self.QueuedValves[i].Mix == 1):

                    SmartBar_Dispenser.PrintFilter.Debug("valve # "+str(self.WaterValveNumber)+" opened  @ dispense time: "+str(self.CurrentDispenseTime-self.DispenseStartTime)+" seconds", SmartBar_Dispenser.PrintFilter.Standard)

                    self.CurrentValveState[self.WaterValveNumber] = 1

                if (self.QueuedValves[i].Mix == 2):

                    SmartBar_Dispenser.PrintFilter.Debug("valve # "+str(self.CarbonatedWaterValveNumber)+" opened  @ dispense time: "+str(self.CurrentDispenseTime-self.DispenseStartTime)+" seconds", SmartBar_Dispenser.PrintFilter.Standard)

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

            SmartBar_Dispenser.PrintFilter.Debug('Valve #'+str(valve_number)+' set for '+str(valve_timing)+' seconds',SmartBar_Dispenser.PrintFilter.Standard)

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

            SmartBar_Dispenser.PrintFilter.Debug(('Maximum valve time for queue: '+str(MaxValveTime)+' seconds'),SmartBar_Dispenser.PrintFilter.Standard)

            return MaxValveTime


    def ClearQueuedValves(self):
        
        del self.QueuedValves

        self.QueuedValves = []

        SmartBar_Dispenser.PrintFilter.Debug('Valve queue cleared',SmartBar_Dispenser.PrintFilter.Standard)


    def TestAllValves(self):

        TestPause = .5
        
        self.ClearQueuedValves()

        for i in range(self.TotalValves):

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

class Print_Filter():

    def __init__(self,system_info_enabled,debugging_info_enabled,warnings_enabled,error_messages_enabled):

        self.SystemInfoEnabled = system_info_enabled # system information on/off
    
        self.DebuggingEnabled = debugging_info_enabled # debugging messages on/off
    
        self.WarningsEnabled = warnings_enabled # display warnings on/off

        self.ErrorMessagesEnabled = error_messages_enabled

        self.Title = 1

        self.SubTitle = 2

        self.Standard = 0

        self.MessageIndicators = ['$',"*","@","&"]

        self.SystemIndex = 0

        self.DebugIndex = 1

        self.WarnIndex = 2

        self.ErrorIndex = 3

        self.MessagePrefix = ["System : ","Debug : ","Warning : ", "Error : "]

        self.Space = " "

        self.NoSpace = ""

        self.MessageBarIndent = 5

        self.MessageBarSize = 125
        

    def System(self, system_message, message_type):
        
        if (self.SystemInfoEnabled == 1):

            if (message_type > 0):

                self.TitleMessage(self.SystemIndex,message_type,system_message)

            else:

                print(system_message+'\n')
            

    def Debug(self, debug_message,message_type):
            
        if (self.DebuggingEnabled == 1):

            if (message_type > 0):

                self.TitleMessage(self.DebugIndex, message_type,debug_message)

            else:

                print(self.MessageIndicators[self.DebugIndex]+self.MessagePrefix[self.DebugIndex]+debug_message+'\n')


    def Warn(self, warning_message, message_type):
            
        if (self.WarningsEnabled == 1):

            if (message_type > 0):

                self.TitleMessage(self.WarnIndex, message_type,warning_message)

            else:

                print(self.MessageIndicators[self.WarnIndex]+self.MessagePrefix[self.WarnIndex]+warning_message+'\n')

            
    def Error(self, error_message, message_type):
            
        if (self.ErrorMessagesEnabled == 1):

           if (self.DebuggingEnabled == 1):

            if (message_type > 0):

                self.TitleMessage(self.ErrorIndex,message_type,error_message)

            else:

                print(self.MessageIndicators[self.ErrorIndex]+self.MessagePrefix[self.ErrorIndex]+error_message)

            
            

    def TitleMessage(self, filter_type, message_type,message_content):

        
        
        MessageLength = len(message_content) + len(self.MessagePrefix[filter_type]) + self.MessageBarIndent + 3*len(self.Space)

        BarString = ""
                                                   
        MessageBar = BarString.join([self.MessageIndicators[filter_type]] * self.MessageBarSize )  # state (open/closed) of all of the valves, initialized to 0 (closed)

        Message = self.Space.join((BarString.join([self.MessageIndicators[filter_type]] * self.MessageBarIndent),self.MessagePrefix[filter_type],message_content,MessageBar[int(MessageLength):]))

        if (message_type == self.Title):

            print('\n'+MessageBar)

            print(Message)

            print(MessageBar)

        else:

            print(Message)
            
        print('\n')


"""
def main():

                                            
    Dispenser = SmartBar_Dispenser()

    Dispenser.ValveManager.ClearQueuedValves()

    Dispenser.ValveManager.ShiftRegisterClear()

    Dispenser.ValveManager.TestAllValves()
    
 #
 #Dispenser.ReceiveCommand("$DO,2,3@W,1,1.5@CO,0,1.5@GA,0,0,3.0@SO,3,1,2.0@SO,1,1,2.0")
  
 #   Dispenser.GPIO_Free()
    
if __name__ == '__main__':
    main()

      

    
            

"""

