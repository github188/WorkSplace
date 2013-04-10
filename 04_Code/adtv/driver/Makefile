ifneq ($(KERNELRELEASE),)
obj-m := Rt10up.o
Rt10up-objs :=Rt10upDriver.o NovelUSB.o 

else
#KDIR?= /lib/modules/$(shell uname -r)/build
KDIR= /home/bakdisk/share/f016/m1-kernel-android/
#KDIR= /root/marvell/amlogic_code_customer1/kernel/
BDIR:= $(shell pwd)
default:
	$(MAKE) -C $(KDIR) M=$(BDIR) modules
.PHONY: clean
clean:
	make -C $(KDIR) M=$(BDIR) clean
endif

