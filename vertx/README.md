# :envelope: TempMail

## TempMail library Vertx


# :mushroom: Deviation sampler
This is `Arduino` library for measure sampling. It can be used when you need to process some data from sensors.
Now it uses sliding window to calculate [Standard deviation] (https://en.wikipedia.org/wiki/Standard_deviation) 

# :mushroom: How it may be usefull?
When you need to detect some fluctuation in the sensor data flow and you don't have the exact value you may compare with 
on each measurement.

# :mushroom: Usage
```c++
#include <Sampler.h>

Sampler sampler = Sampler();

void setup() {
    pinMode(0, INPUT);
}

void loop() {
    int sensorValue = analogRead(0);
    sampler.sample(sensorValue);
    int deviation = sampler.deviation();
    if (deviation > 1) {
        //here we go, some fluctuation in data, do something
    }
}
```

# :mushroom: How to install
Just copy Sampler folder (with two files inside, Sampler.h and Sampler.cpp) into you Arduino SDK install folder/libraries