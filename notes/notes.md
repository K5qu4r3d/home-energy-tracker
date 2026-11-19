# How electrical usage works
- Power: how much energy used per second, in W or kW
- Energy: how much total energy used over time, in kWh
- Price per kWh used

## Examples:
- Hair dryer uses 1200 W or 1.2 kW
  - 1 hr usage: 1.2 kW * 1 hr = 1.2 kW/hr or kWh
  - 1 min usage: 1.2 kW * (1/60) hr = 0.02 kWh
- Hair dryer is used in a building where its electric price is 0.30 GBP/kWh
  - Cost for 1 hr: 1.2 kWh * 0.30 GBP/kWh = 0.36 GBP
  - Cost for 1 min: 0.02 kWh * 0.30 GBP/kWh = 0.006 GBP

# System Requirements
- Track daily/hourly energy consumption per device or room
- View trends over time
- Get alerts for anomalies (e.g. high consumption)
- Receive AI-powered tips to reduce energy usage
- Simulate or accept IoT/sensor data in real time

## Assumptions
- Users: 500,000
- 5 devices per user
- Readings per device: 1 per minute
- Each reading → 1 database insert

## Calculations
- Total number of devices = 500k * 5 = 2.5 mil
- Readings per minute = 2.5 mil
- Readings per day = 2.5 mil/min * 1,440 min = 3.6 bil
- Size of each reading = 72 bytes
- Storage required per day = 3.6 bil * 72 bytes = 259 GB
- Storage required per month = 8 TB
  - 4 TB when compressing half of the data
- Write throughput = 2.5 mil / 60 sec = 41.6k inserts/sec