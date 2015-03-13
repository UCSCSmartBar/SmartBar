import sys
sys.path.append('./GitSmartBar/System/')
import time 
import RPi.GPIO as gpio
import analogspi as ADC
import Printer as dataprinter
import threading


BACDetector_NumReadsPerSample = 12
BACDetector_SampleReadInterval = .25 #
BACDetector_GreenLEDIndex = 3
BACDetector_MinTimeBetweenSamples = 20
BACDetector_BlueLEDIndex = 1
BACDetector_GreenLEDIndex = 2
BACDetector_RedLEDIndex = 3

BACShifter_SR_Data = 16 # pin number of serial output to shift register
    
BACShifter_SR_Clock = 20 # pin number of clock output to shift register

BACShifter_SR_Store = 21

BACShifter_SR_ClockPause = .0000001
BACShifter_SR_BitsInUse = 4
BACShifter_SR_TotalBitsAvailable = 8

class SmartBar_BACDetector:

    def __init__(self): # set up gpio, set up variables based on the total amount of valves being used
   
        gpio.setmode(gpio.BCM)
        gpio.setwarnings(False)
        self.SR_Data = BACShifter_SR_Data
        self.SR_Clock = BACShifter_SR_Clock
        self.SR_Store = BACShifter_SR_Store
        self.SR_ClockPause = BACShifter_SR_ClockPause
        self.SR_BitsInUse = BACShifter_SR_BitsInUse
        self.SR_TotalBitsAvailable = BACShifter_SR_TotalBitsAvailable
        gpio.setup(self.SR_Data,gpio.OUT) #red
        gpio.setup(self.SR_Clock,gpio.OUT) #blue
        gpio.setup(self.SR_Store,gpio.OUT) #greenGP
        ADC.InitSPI();
        self.IOStatus = [0,1,1,1] # [sensor, red led, green led, blue led]
        self.Printer = dataprinter.PrintLogFilter(1,1,1,1,"BACDetector","./BAC_Detector_Logs",2)
        self.TotalNumberOfSensorReads = 0 # number of times the sensor has been read since it was turned on
        self.InitializationSuccessful = 0
        #test variables
        self.TestValueIndex = 0
        self.TotalNumberOfSensorReads = BACDetector_NumReadsPerSample
        BACDetector_SampleReadInterval #  0 # number of times the sensor has been read since it was turned on
        self.SensorReadSensorInterval = BACDetector_SampleReadInterval # flag indicating if the sensor has been initialized and warmed up properly
        self.NumSamplesCollected =  0
        self.SampleSensorData = [-1]*self.TotalNumberOfSensorReads
        self.NextPossibleSampleTime = 0
      #  self.InitializeBACSensor()

    def InitializeBACSensor(self):
        # constants
        self.Printer.System("Starting BAC Sensor Initialization/Warm up",self.Printer.Title)
        self.InitalizeSampleTolerance = .2 # voltage that all samples are within +/- of each other
        self.NumberOfSamplesForSuccess = 10 # number of samples in a row that are not decreasing and that are within a certain range of each other indicating that the sensor is fully warmed up
        self.InitializeSampleInterval = 2 # time between samples
        self.RecentInitializeData = [-1]*self.NumberOfSamplesForSuccess # array containing most recent samples
        self.NumberOfNonDecreasingSamples = 5 # number of samples to consider initialization successful out of the most recent that did not decrease since the last readingon
        WarmUpMonitor = threading.Timer(self.InitializeSampleInterval,self.BACSensorWarmUpMonitor)
        WarmUpMonitor.start()
    def BACSensorWarmUpMonitor(self):
        self.BACSensorValue = self.ReadBACSensorTest()
        if (self.TotalNumberOfSensorReads < self.NumberOfSamplesForSuccess):
            self.RecentInitializeData[self.TotalNumberOfSensorReads] = self.BACSensorValue
        else:
            self.RecentInitializeData.pop()
            self.RecentInitializeData.insert(0,self.BACSensorValue)
            self.AnalyzeInitializaData()
        self.Printer.System("Current Sensor Value: "+str(self.BACSensorValue)+" - Warm Up Sample #"+str(self.TotalNumberOfSensorReads),self.Printer.Standard)
        self.AnalayzeInitialData()    

    def AnalayzeInitialData(self):
        if (self.InitializationSuccessful != 1):
            WarmUpMonitor = threading.Timer(self.InitializeSampleInterval,self.BACSensorWarmUpMonitor)
            WarmUpMonitor.start()
        print('word')
        
    def ReadBACSensor(self):

        BACSensorValue = str(ADC.ReadChannel(0))
        return BACSensorValue


    def TurnBACSensorOnOff(self, on_or_off):
        self.IOStatus[0] = on_or_off
        self.UpdateShiftRegisters()
        
    def TurnBACLEDOnOff(self,led_index,on_or_off):
        self.IOStatus[led_index] = 1-on_or_off
        self.UpdateShiftRegisters()



    def ShiftRegisterClear(self): # clear shift registers - set all bits to 0
        gpio.output(self.SR_Data,gpio.LOW) # shift register serial input set low
        for i in range(self.SR_TotalBitsAvailable): # shift in all low values
	    self.ShiftRegisterTick() # shift clock cycle
	self.ShiftRegisterStore() # required extra clock cycle
	

    def ShiftRegisterStore(self): # shift register clock cycle
        
        gpio.output(self.SR_Store,gpio.HIGH) # set rising clock edge
        CurrentClockTime = time.time()
        EndClockTime = (time.time() + self.SR_ClockPause)
        while(CurrentClockTime < EndClockTime):
            CurrentClockTime = time.time()
        gpio.output(self.SR_Store,gpio.LOW) # set falling clock edge

        

    def UpdateShiftRegisters(self): # update shift registers by shifting in state data
        i = (self.SR_TotalBitsAvailable - 1) # index for the total number of bits available to the shift register
        while(i >= 0): # counting down because the last values are shifted in first
            if (i < (self.SR_BitsInUse)):
                if (self.IOStatus[i] > 0):   
                    gpio.output(self.SR_Data,gpio.HIGH) 
                else: # write a low if the valve is not supposed to be open
                    gpio.output(self.SR_Data,gpio.LOW)
            else:
                gpio.output(self.SR_Data,gpio.LOW) 
            i = i-1
            self.ShiftRegisterTick() # clock cycle to shift the bits along
        self.ShiftRegisterStore()

            
        
    def ShiftRegisterTick(self): # shift register clock cycle
        gpio.output(self.SR_Clock,gpio.HIGH) # set rising clock edge
        CurrentClockTime = time.time()
        EndClockTime = (time.time() + self.SR_ClockPause)
        while(CurrentClockTime < EndClockTime):
            CurrentClockTime = time.time()   
        gpio.output(self.SR_Clock,gpio.LOW) # set falling clock edge



    def ReadBACSensorTest(self):

        TestBACValues = [3.3,3.2,3.1,3.0,2.9,2.8,2.7,2.6,2.5,2.45,2.4,2.35,2.30,2.25,2.22,2.20,2.19,2.19,2.20]
        TestBACSensorValue = TestBACValues[self.TestValueIndex]
        self.TestValueIndex = self.TestValueIndex +1
        return TestBACSensorValue

    

    

    def CollectBACSample(self):
        if (self.NextPossibleSampleTime < time.time() ):
            self.Printer.System("Starting Bac Sample Collect",self.Printer.Title)
            self.BACBlinkLED()
            self.TotalOfReadValues = 0
            BACSampleStartTime = time.time()
            self.TurnBACLEDOnOff(BACDetector_GreenLEDIndex,1)
            self.Printer.System("Reading Sensor",self.Printer.SubTitle)
            for i in range(BACDetector_NumReadsPerSample):
                self.SampleSensorData[i] = int(self.ReadBACSensor())
                self.TotalOfReadValues = self.TotalOfReadValues+self.SampleSensorData[i]
                CurrentClockTime = time.time()
                TimeUntilSample = CurrentClockTime + self.SensorReadSensorInterval
                self.Printer.System("Sensor Value: "+str(self.SampleSensorData[i]),self.Printer.Standard)
                while(CurrentClockTime <TimeUntilSample):
                    CurrentClockTime = time.time()
            self.TurnBACLEDOnOff(BACDetector_GreenLEDIndex,0)
            self.TurnBACLEDOnOff(BACDetector_RedLEDIndex,1)
            self.Printer.System("Analyzing Sensor Readings",self.Printer.SubTitle)
            self.BACReadsAverage = self.TotalOfReadValues/BACDetector_NumReadsPerSample
            self.Printer.System("Average of Sensor Readings : "+str( self.BACReadsAverage),self.Printer.Standard)
            self.NextPossibleSampleTime = time.time() + BACDetector_MinTimeBetweenSamples
            self.TurnBACLEDOnOff(BACDetector_RedLEDIndex,1)
    def BACBlinkLED(self):

        NumberOfBlinks = 3
        OnDuration = .75
        OffDuration = .25
        self.TurnBACLEDOnOff(BACDetector_RedLEDIndex,0)
        self.TurnBACLEDOnOff(BACDetector_BlueLEDIndex,1)
        self.TurnBACLEDOnOff(BACDetector_GreenLEDIndex,0)
        for i in range(NumberOfBlinks):
            self.TurnBACLEDOnOff(BACDetector_BlueLEDIndex,1)
            BlinkStartTime = time.time()
            while((OnDuration+BlinkStartTime)> time.time()):
                pass
            self.TurnBACLEDOnOff(BACDetector_BlueLEDIndex,0)
            while((OnDuration+BlinkStartTime+OffDuration) >time.time()):
                pass
            
        


