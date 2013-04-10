ifneq ($(KERNELRELEASE),)
obj-m := maindvb.o
Rt10up-objs :=MyService.o iMyService.o 

else
#KDIR := /home/a/kernel-YS/linux-2.6.28-tcc/
#KDIR?= /home/a/opt/kernel
#KDIR?= /lib/modules/$(shell uname -r)/build
KDIR= /home/administrator/newdisk/host/workspace/lichee/linux-2.6.36

BDIR:= $(shell pwd)
default:
	$(MAKE) -C $(KDIR) M=$(PWD) modules
.PHONY: clean
clean:
	make -C $(KDIR) M=$(BDIR) clean
endif

