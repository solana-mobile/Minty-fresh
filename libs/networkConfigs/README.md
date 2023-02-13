## NetworkConfig Module

Roles & Responsibilities:

+ Binds NetworkInterface to a concrete implementation.
+ This module can be used to switch to a different implementation of NetworkInterface.
  + Example of this could be to use a different RPC provider such as alchemy or helius.
  + Use a different implementation to get a list of all Mints, maybe using a different RPC provider.