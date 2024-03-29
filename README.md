
# Torino Electric Vehicle Simualtion on Large Scale Network using MATSim

The number of electric vehicles (EVs) in developed countries is proliferating. This increase in EV leads to operational challenges, including daily energy requirements, charging infrastructure and battery size design. An agent-based approach is adopted for modelling multi-day traffic data using floating car data to detect EV's trip-chains. The paper comprises two main sections: firstly, the road network calibration process has resulted in more reliable travel time and speed estimation per trip. As a result, the squared error of simulated average speed per Origin-Destination is reduced by 50% compared with floating car data. Secondly, a baseline scenario with current and innovative battery sizes is simulated for a week. Additionally, energy consumption model for vehicles considering average speed and road slope in the cumulative estimation of the energy required during travel. Finally, the spatial results of energy consumption, charging, and waiting time at charging stations over a week are calculated.




## Roadmap

- Floating Car data for agents plans

- Network loading incoperating road slope

- Electric Vehicle Segmentation (A,B,C,D,E)

- Efficient abstracted road network by creating buffer

- Incoperating Public charging Station from OpenCharge Api

- Home Charging induced whenever vehicle is in Idle state


## Screenshots

![Network](/network.PNG)

![Network](/vehicle%20atr%208.PNG)
![Charging Location](/charging_locations.PNG)



## Authors

- [@praveen kumar pandey](https://www.github.com/prvnpandey)

## Simulation

![Simulation Demo](/final_video.mp4)


## Licenses


(The following paragraphs need to be adjusted according to the specifications of your project.)

The MATSim program code in this repository is distributed under the terms of the GNU General Public License as published by the Free Software Foundation (version 2). The MATSim program code are files that reside in the src directory hierarchy and typically end with *.java.

The MATSim input files, output files, analysis data and visualizations are licensed under a Creative Commons Attribution 4.0 International License. Creative Commons License
 MATSim input files are those that are used as input to run MATSim. They often, but not always, have a header pointing to matsim.org. They typically reside in the scenarios directory hierarchy. MATSim output files, analysis data, and visualizations are files generated by MATSim runs, or by postprocessing. They typically reside in a directory hierarchy starting with output.

Other data files, in particular in original-input-data, have their own individual licenses that need to be individually clarified with the copyright holders.


## Feedback

If you have any feedback, please reach out to us at prvnpandey2010@gmail.com




