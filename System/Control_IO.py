#!/usr/bin/
# author: michael sierra
import sys
sys.path.append('../Dispenser/')
sys.path.append('../System/')
sys.path.append('../Fingerprint_Code/pyGT511C3-maseter/pyGT511C3')
import RPi.GPIO as GPIO
#import Finger_Print as FP
#import analogspi as spi
import DispensingSystem as DS
import analogspi as SPI
import DispensingSystem as DS
import FingerprintGet as FPQ
import usbcomm as USB
import time
from threading import Thread
#import thread
#import FingerprintStateMachine as FPSM

RedLEDPin = 16
BlueLEDPin = 20
GreenLEDPin = 21
#cuppin = 23

# param: 
# return: 
# brief: initializes GPIO, setting appropriate pins.
def CIO_Initialize():
    GPIO.setmode(GPIO.BCM)
    #GPIO.setup(26, GPIO.OUT)
    #GPIO.setup(20, GPIO.OUT)
    #OnboardLED
    GPIO.setup(RedLEDPin,GPIO.OUT) #red
    GPIO.setup(BlueLEDPin,GPIO.OUT) #blue
    GPIO.setup(GreenLEDPin,GPIO.OUT) #green
    #GPIO.setup(cuppin,GPIO.IN)
    #spi.InitSPI()
    print('GPIO initialized')
    SPI.InitSPI()
    print('SPI initialized')
    FPQ.fpget_init()
    print('FPFS initialized')
    FPQ.fps = FPQ.fps_init()


# param: 
# return: 
# brief: closes GPIO   
def CIO_Free():
    print 'goodbye -love gpio'
    GPIO.cleanup()
    FPQ.clear_files()
# param
# return
 #brief: Test AD

def Test_SPI():
    CIO_Initialize()
    ADstr = '$AD.' + str(spi.ReadChannel(0)) 
    print ADstr
'''
def cupdetect():
    if not hasattr(cupdetect,'lastval'):
        cupdetect.lastval = 0
    if (GPIO.input(cuppin)):
        if cupdetect.lastval == 0
            print 'cupdetect: cup placed'
            cupdetect.lastval = 1
    else:
        if cupdetect.lastval == 1
            print 'cupdetect: cupremoved'
            cupdetect.lastval = 0
    return cupdetect.lastval
'''
# param: string: string to be parsed
# return: string to be sent back to UI
# brief: Parses the string and calls appropriate function.
def Parse_Message(string,ldev):

    if string == '$Error':
        return '$ACK|Error'
    elif string == '$Warn':
        return 'Warning'
    else:
        print '->>' + string
        sList = string.split(",")
        #print sList
        if (sList[0] == '$DO') :
            '''
            print 'please place cup'
            while not cupdetect():
                pass
            '''
            if not hasattr(Parse_Message,'Dispenser'):
                Parse_Message.Dispenser = DS.SmartBar_Dispenser()
                TempPause = time.time() + 2
                Temp = time.time()
                while(Temp < TempPause):
                    Temp = time.time()
            Parse_Message.Dispenser.ReceiveCommand(string)
            print('hi mom')
        elif (sList[0] == '$SYS'):
            return '$ACK'
        elif (sList[0] == '$FP'):
            print("Acknowledged Finger Print Command")
            FP.Finger_PrintSM(sList[1])
            return '$ACK,DO'
        elif (sList[0] == '$AD'):               #returns ADC value
            ADstr = '$AD.' + str(spi.ReadChannel(sList[1]))            
            return '$ACK,AD' +ADstr
        
        elif (sList[0] == '$FPQ'):
            if FPQ.update_queue(string):
                return '$ACK,FPQ'
            else:
                return '$NAK,NOTINQ'
        elif (sList[0] == '$FPENROLL'):
            print('C_IO ENROLL')
            msg = FPQ.enroll_ID(int(sList[1]),ldev)
            return msg
        elif (sList[0] == '$FPIDEN'):
            if (sList[1] == 'START'):
                print('CIO_FPIDEN Start')
                #thread.start_new_thread(fp.identify, (ldev,))
                fpthd = Thread( target=FPQ.identify, args=(ldev,))
                fpthd.start()
                return '$FPIDEN,STARTED'
        else:
            return '$NAK'

