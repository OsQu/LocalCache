Server
======

Installation
------------

Install [nodejs](http://nodejs.org/) (0.10) using a method whatever works in your system.

Then install the dependencies with

    $ npm install

Running
-------

Now you can start the server with

    $ node app.js fetchtime  
    Where 'fetchtime' should be a number less than 24 and greater or equal to 0. The fetch time is the time when the app will try to update for new data from the HS server every day.

Tests
-----
http://127.0.0.1:8080                ,this will render the whole json string
http://127.0.0.1:8080/{pictureid}    ,this will render the pictures with the given pictureid

