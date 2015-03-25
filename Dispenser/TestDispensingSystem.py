import DispensingSystem as SBDispenser


class DispensingSystemTest():
    def __init__(self):
        self.Dispenser = SBDispenser.SmartBar_Dispenser()    

    def ReceiveDrinkOrderTest(self):
        self.Dispenser.ReceiveCommand("$DO,2,2@WH,1,0.1@GN,1,0.1@GT,1,0,0.1@TO,1,0,0.")
        self.Dispenser.ReceiveCommand("$DO,6,2@WH,1,0.1@GN,1,0.1@GT,1,0,0.1@TO,1,0,0.")
        self.Dispenser.ReceiveCommand("$DO,2,2@GN,1,0.1@GT,1,0,0.1@TO,1,0,0.2")
        self.Dispenser.ReceiveCommand("$GI")
        self.Dispenser.InventoryManager.UserSetInventory("$IV,2,2@0,ZZ,1,54.2,59.2@1,QQ,1,49.8,59.2@2,TO,1,125.5,128.0@3,HH,1,123.0,128.0")
        self.Dispenser.ReceiveCommand("$GI")
def main():

                                            
    Tester = DispensingSystemTest()
    Tester.ReceiveDrinkOrderTest()


 #   Dispenser.ReceiveCommand("$DO,2,2@GN,1,1.0@WH,1,0.1@GT,1,0,0.1@TO,1,1,0.5")
 #   Dispenser.GPIO_Free()
    
if __name__ == '__main__':
    main()

