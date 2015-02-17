#!/usr/bin/python
# author: michael sierra

import usb.core
import usb.util
import struct
import time
import thread
import os
import socket
import random
import sys
import RPi.GPIO as GPIO
import Control_IO as CIO
import atexit

#on exit, gpio is freed for other applications
atexit.register(CIO.CIO_Free)

ACCESSORY_VID = 0x18D1
#ACCESSORY_PID = (0x2D00, 0x2D01, 0x2D04, 0x2D05)
ACCESSORY_PID = 0x2D01
VID_NEXUS = 0x18d1
PID_NEXUS = 0xd002
VID_GALAXY = 0x04e8
PID_GALAXY = 0x6860
VID_RCA = 0x0414
PID_RCA = 0x0C02

# param: none
# return: dev, device descriptor for usb
# brief: searches for android device in accessory mode. Returns none if not found
def get_accessory():
    dev = usb.core.find(idVendor = ACCESSORY_VID,
                        idProduct = ACCESSORY_PID)
    return dev

# param: none
# return: dev, device descriptor for usb
# brief: searches for nexus 4 in usb.
def get_nexus4():
    print('Looking for nexus 4')
    print('VID: 0X%0.4x - PID: 0x%0.4x' % (VID_NEXUS, PID_NEXUS))
    #android_dev = usb.core.find(idVendor=VID_NEXUS)
    android_dev = usb.core.find(idVendor=VID_NEXUS, idProduct = PID_NEXUS)
    if android_dev:
        print('Nexus Found')
    else:
        print('error:')
    return android_dev

# param: none
# return: dev, device descriptor for usb
# brief: searches for rca tablet in usb.
def get_rca():
    print('Looking for RCA')
    print('VID: 0X%0.4x - PID: 0x%.4x' % (VID_RCA, PID_RCA))
    android_dev = usb.core.find(idVendor=VID_RCA, idProduct = PID_RCA)
    if android_dev:
    	print('RCA_found')
    else:
    	print('Device not found')
    return android_dev

# param: ldev, device to set protocol
# return: 
# brief: checks if android device is compatible with AOA 
def set_protocol(ldev):
    print('setting protocol')
    try:
        ldev.set_configuration()
    except usb.core.USBError as e:
        if e.errno == 16:
            print('device configured')
        else:
            sys.exit('config failed')
    ret = ldev.ctrl_transfer(0xC0, 51, 0, 0, 2)
    protocol = ret[0]
    if protocol < 2:
        print('poop')
        sys.exit('protocol v2 not supported')
    print 'protocol support up to V' + str(protocol)
    return

# param: ldev, device to set string
# return: 
# brief: sets the strings to open the appropriate app on the tablet.
def set_strings(ldev):
        send_string(ldev, 0, 'SmartBar')            #manufacturer
        send_string(ldev, 1, 'PiAccessory')         #model
        send_string(ldev, 2, 'Smart Bar App')       #description
        send_string(ldev, 3, '0.2.0')               #version number
        send_string(ldev, 4, 'ucscsmartbar.com')    #url
        send_string(ldev, 5, '0001')                #serial
        return

# param: ldev: device to send string; str_id: which string to interpret;
#        str_val: string o send
# return:
# brief: sends the strings with a ctrl_transfer
def send_string(ldev, str_id, str_val):
    ret = ldev.ctrl_transfer(0x40, 52, 0, str_id, str_val, 0)
    if ret != len(str_val):
        sys.exit('failed to send string %i' % str_id)
    return

# param: ldev, device to set string
# return:
# brief: puts android in accessory mode.
def set_accessory_mode(ldev):
    ret = ldev.ctrl_transfer(0x40, 53, 0, 0, '', 0)
    if ret:
        sys.exit('startup failed')
    else:
        print('set accessory mode successful')
    time.sleep(1)   #gives the android time to process
    return

# param: none
# return: dev, device descriptor for usb
# brief: run through the steps to set accessory mode.
def start_accessory_mode():
    dev = get_accessory()
    if not dev:
        print('android accessory not found')
        print('try to start accessory mode')
        dev = get_nexus4()
        set_protocol(dev)
        set_strings(dev)
        set_accessory_mode(dev)
        dev = get_accessory()
        if not dev:
            sys.exit('unable to start accessory mode')
        print('accessory mode successful')
    else:
        print('already in accessory mode')
    return dev

# param: none
# return: dev, device descriptor for usb
# brief: continually run through the steps to set accessory mode.
def wait_for_accessory():
    GPIO.output(19, 1)
    dev = get_accessory()
    while not dev:
        print('android accessory not found')
        print('trying to start accessory mode')
        #dev = get_nexus4()
        dev = get_rca()
        if dev:
            set_protocol(dev)
            set_strings(dev)
            set_accessory_mode(dev)
            dev = get_accessory()
            print('accessory mode succesful')
            GPIO.output(19, 0)
            return dev
            break
        else:
            print('couldn\'t find dev')
            time.sleep(2)
    else:
        print('already in accessory mode')
    GPIO.output(19, 0)
    return dev
                      
# param: ldev, device descriptor for usb
# return: 
# brief: reads usb buffer from ldev, then calls a function to parse
#        command
def readbuf(ldev):
    try:
        ret = ldev.read(0x81, 15)
        if ret:
            GPIO.output(16, 0)
            GPIO.output(21, 1)
        sret = ''.join([chr(x) for x in ret])
        var = CIO.Parse_Message(sret)
        writebuf(ldev, var)
        print var
        print('>>> ' + sret)
    except usb.core.USBError as e:
        print 'readbuf error' + str(e)
    except:
        pass
    time.sleep(.02)
    GPIO.output(16, 1)
    GPIO.output(21, 0)

# param: ldev, device descriptor for usb; msg, message to send
# return: 
# brief: writes to usb buffer in ldev
def writebuf(ldev, msg):
    print('<<< ' + msg + '\n'),
    try:
        ret = ldev.write(0x02, msg)
        if ret == len(msg):
            pass
        else:
            print('ret != msg')
    except:
        print('writebuf error')

# param: ldev, device descriptor for usb
# return: 
# brief: continually reads buffer while device is connected
def readbufThread(ldev):
    print('starting thread')
    while get_accessory():
        readbuf(ldev)
    print('thread ended')


def main():
    CIO.CIO_Initialize()
    dev = wait_for_accessory()
    GPIO.output(16, 1)
    if dev:
        time.sleep(.02)
        writebuf(dev, '$ready')
        try:
            thread.start_new_thread(readbufThread, (dev,))
        except Exception as e:
            print 'error' + str(e)
        while True:
            try:
                if not get_accessory():     #disconnected
                    GPIO.output(16, 0)
                    GPIO.output(19, 1)
                    dev = wait_for_accessory()
                    if dev:
                        thread.start_new_thread(readbufThread, (dev,))
                        time.sleep(.02)
                        writebuf(dev, '$ready')
            except KeyboardInterrupt:
                print ('To android: ')
                msg = raw_input()
                if msg == 'exit':
                    sys.exit()
                writebuf(dev, msg)
            except:
                pass
        GPIO.output(16, 0)
        
if __name__ == '__main__':
    main()

