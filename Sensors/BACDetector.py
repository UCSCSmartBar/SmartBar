import sys
sys.path.append('./GitSmartBar/System/')
sys.path.append('..')
import time 
import RPi.GPIO as gpio
import analogspi as ADC
import Printer as Print
import threading


BACDetector_NumReadsPerSample = 12
BACDetector_SampleReadInterval = .25 #
BACDetector_ADCChannel = 0

class SmartBar_BACDetector:

    def __init__(self): # set up gpio, set up variables based on the total amount of valves being used
   
        gpio.setmode(gpio.BCM)
        gpio.setwarnings(False)
        ADC.InitSPI();

        self.Printer = Print.PrinterLogger(1,1,1,1,"BACDetector",2)

        #test variables
        self.TotalNumberOfSensorReads = BACDetector_NumReadsPerSample
        BACDetector_SampleReadInterval #  0 # number of times the sensor has been read since it was turned on
        self.SensorReadSensorInterval = BACDetector_SampleReadInterval # flag indicating if the sensor has been initialized and warmed up properly
        self.NumSamplesCollected =  0
        self.SampleSensorData = [-1]*self.TotalNumberOfSensorReads

        
    def ReadBACSensor(self):

        BACSensorValue = str(ADC.ReadChannel(BACDetector_ADCChannel))
        return BACSensorValue    

    def CollectBACSample(self):
            
            self.Printer.System("Starting Bac Sample Collect",self.Printer.Title)
            self.TotalOfReadValues = 0
            BACSampleStartTime = time.time()
            self.Printer.System("Reading Sensor",self.Printer.SubTitle)
            for i in range(BACDetector_NumReadsPerSample):
                self.SampleSensorData[i] = int(self.ReadBACSensor())
                self.TotalOfReadValues = self.TotalOfReadValues+self.SampleSensorData[i]
                CurrentClockTime = time.time()
                TimeUntilSample = CurrentClockTime + self.SensorReadSensorInterval
                self.Printer.System("Sensor Value: "+str(self.SampleSensorData[i]),self.Printer.Standard)
                while(CurrentClockTime <TimeUntilSample):
                    CurrentClockTime = time.time()
            self.Printer.System("Analyzing Sensor Readings",self.Printer.SubTitle)
            self.BACReadsAverage = self.TotalOfReadValues/BACDetector_NumReadsPerSample
            self.Printer.System("Average of Sensor Readings : "+str( self.BACReadsAverage),self.Printer.Standard)

    def ReadBACSensor(self):

        BACSensorValue = str(ADC.ReadChannel(0))
        return BACSensorValue   

# Functions in Development
'''

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

'''
