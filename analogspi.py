import sys
import spidev
import time
import os


def InitSPI():
    if not hasattr(InitSPI, "spi"):
        InitSPI.spi = 0
    InitSPI.spi = spidev.SpiDev()
    InitSPI.spi.open(0,0)
    print 'spi initialized'
    return

def ReadChannel(channel):
    adc = InitSPI.spi.xfer2([1,(8+channel)<<4,0])
    data = ((adc[1]&3) << 8) +adc[2]
    return data

