package no.uio.ifi.team16.stim.data

data class Grid(val id : Int)
data class InfectiousPressure(val concentration : Grid,
                              val eta_rho : List<Float>,
                              val xi_rho : List<Float>,
                              val gridMapping : Int,
                              val latitude : Grid,
                              val longitude : Grid)