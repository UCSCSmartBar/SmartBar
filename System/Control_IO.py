#!/usr/bin/
# author: michael sierra
import sys
sys.path.append('/SmartBar/Dispenser/')
sys.path.append('/SmartBar/System/')
import RPi.GPIO as GPIO
#import Finger_Print as FP
import analogspi as spi
import DispensingSystem as DS
import analogspi as SPI
import DispensingSystem as DS
import FingerprintGet as FPQ
import time
import thread


# param: 
# return: 
# brief: initializes GPIO, setting appropriate pins.
def CIO_Initialize():
    GPIO.setmode(GPIO.BCM)
    #GPIO.setup(26, GPIO.OUT)
    #GPIO.setup(20, GPIO.OUT)
    #OnboardLED
    GPIO.setup(16,GPIO.OUT) #red
    GPIO.setup(20,GPIO.OUT) #blue
    GPIO.setup(21,GPIO.OUT) #green
    spi.InitSPI()
    print('GPIO initialized')
    SPI.InitSPI()
    print('SPI initialized')
    FPQ.fpget_init()
    print('FPFS initialized')

# param: 
# return: 
# brief: closes GPIO   
def CIO_Free():
    print 'goodbye -love gpio'
    GPIO.cleanup()
# param
# return
 #brief: Test AD

def Test_SPI():
    CIO_Initialize()
    ADstr = '$AD.' + str(spi.ReadChannel(0)) 
    print ADstr

# param: string: string to be parsed
# return: string to be sent back to UI
# brief: Parses the string and calls appropriate function.
def Parse_Message(string):
    if string == '$Error':
        return '$ACK|Error'
    elif string == '$Warn':
        return 'Warning'
    else:
        print string
        sList = string.split(",")
        #print sList
        if (sList[0] == '$DO') :
            Dispenser = DS.SmartBar_Dispenser()
            Dispenser.ReceiveCommand(string)
            print('hi mom')
            del Dispenser
        elif (sList[0] == '$AD'):               #returns ADC value
            print '<--AD'
            ADstr = '$AD.' + str(spi.ReadChannel(sList[1]))  
            print ADstr          
            return ADstr
        elif (sList[0] == '$FP'):
            print("Acknowledged Finger Print Command")
            FP.Finger_PrintSM(sList[1])
            return '$ACK|DO'
        elif (sList[0] == '$AD'):               #returns ADC value
            ADstr = '$AD.' + str(spi.ReadChannel(sList[1]))            
            return '$ACK|AD'
        
        elif (sList[0] == '$FPQ'):
            if FPQ.get_files(string):
                return '$ACK|FPQ'
            else:
                return '$NAK.NOTINQ'
        else:
            return '$NAK'
