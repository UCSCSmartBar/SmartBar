import BACDetector
import time as t

class BACSystemTest():
    def __init__(self):
        BACSystemTest.BACDetect = BACDetector.SmartBar_BACDetector()    

    def TestGetBACValue(self):
        self.BACDetect.CollectBACSample()


            
def main():

                                            
    Tester = BACSystemTest()
    Tester.TestGetBACValue()


    
if __name__ == '__main__':
    main()


