Virtual Machine
===============

This is a virtual machine intended for development usage. It is run with [Vagrant](https://www.vagrantup.com/) and [VirtualBox](https://www.virtualbox.org/) (both free) and tries to be as similar as possible than the KVM environment that will be deployed to RACS.

Installation
------------

Install pre-requisites Vagrant and VirtualBox. Then run:

    $ Todo

Provisioning
------------

The VM is provisioined using [Chef](https://www.chef.io/chef/), but a developer shouldn't have to worry about provisoining.

Deploying to RACS
-----------------

Or the title should be "Why not to use libvirt and QEMU right away?"

Since we are not doing anything fancy, creating the RACS-compatitible image should be pretty straightforward. If everything else fails, we can always provision the box manually with libvrt and QEMU. However, since Vagrant ships with VirtualBox support, it is easier for development just to use the stock options.
