# Autonomous Air Traffic Control
This repository contains the soruce code from my undergraduate dissertation project: Multi-Agent Path Finding Techniques for Autonomous Air Traffic Control.

## Instructions
A full [user manual](Manual.pdf) is provided that explains how to run the air traffic simulator and automate the air traffic control. Detailed information about the project architecture and implementation is provided in the full disertation text.

## Abstract
The aviation industry currently employs teams of highly skilled air traffic controllers to control the movement of air traffic and prevent mid-air collisions from happening. The men and women who perform this duty spend years training to do their job, but they are only human and can only handle a certain level of workload. The key aim of this project is to assess the feasibility of using a multi-agent path finding (MAPF) algorithm to automate the job of air traffic controllers.

A MAPF algorithm will calculate paths for agents (the aircraft) from a start location to a goal location on a graph, while maintaining a safe separation between them. There are many algorithms which can achieve this, each has its own pros and cons, however, the algorithm which this project uses is called Windowed Hierarchical Cooperative A* (WHCA*). This algorithm will find partial paths for agents within a windowed time period, which means it can adapt to a changing environment.

The project involves the development of two key components: a simulator to simulate the movement of aircraft, and an implementation of WHCA* to automate the control of aircraft agents. The project finally evaluates the effectiveness of different parameters and techniques, as well as the overall performance compared to human air traffic controllers.

Overall, the MAPF technique is effective at safely keeping aircraft separated, however, the limitation of the graph prevents more optimal routes for aircraft been found. With further research and development, the MAPF approach could be a feasible method for automating air traffic control.

*The full dissertation text is available in this repository.*
