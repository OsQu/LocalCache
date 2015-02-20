Virtual Machine
===============

This is a virtual machine intended for development usage. It is run with [Vagrant](https://www.vagrantup.com/) and [VirtualBox](https://www.virtualbox.org/) (both free) and tries to be as similar as possible than the KVM environment that will be deployed to RACS.

Installation
------------

Fetch git submodules:

    $ git submodule update --init

Install pre-requisites Vagrant and VirtualBox. Then run:

    $ vagrant plugin install vagrant-omnibus

to install `vagrant-omnibus` plugin that is used for provisioining.

To start the server, type:

    $ vagrant up

After the server is installed and provisioined, you should be able to access it with

    $ vagrant ssh

`server` folder is already mounted to `/etc/localcache`, so you can `cd` to there and start the node app. `Vagrantfile` already contains portforwarding from :8080 (host) -> 80 (quest), so you should be able to start the Node server and access it in your computer with:

    $ cd /etc/localcache
    $ sudo PORT=80 /usr/local/bin/node app.js

Now open http://localhost:8080 at your browser.

Provisioning
------------

The VM is provisioined using [Chef](https://www.chef.io/chef/), but a developer shouldn't have to worry about provisoining.

Deploying to RACS
-----------------

Or the title should be "Why not to use libvirt and QEMU right away?"

Since we are not doing anything fancy, creating the RACS-compatitible image should be pretty straightforward. If everything else fails, we can always provision the box manually with libvrt and QEMU. However, since Vagrant ships with VirtualBox support, it is easier for development just to use the stock options.
