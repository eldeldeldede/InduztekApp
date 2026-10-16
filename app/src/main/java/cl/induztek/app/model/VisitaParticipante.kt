package cl.induztek.app.model

data class VisitaParticipante(
    val idVisita: Long,
    val idUsuario: Long // id del usuario del trabajador de Induztek que estuvo en la visita
)
