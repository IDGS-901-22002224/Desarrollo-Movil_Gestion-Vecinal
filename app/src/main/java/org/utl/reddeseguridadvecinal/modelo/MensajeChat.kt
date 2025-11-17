package org.utl.reddeseguridadvecinal.modelo

import com.google.firebase.database.ServerValue
// NO import com.google.firestore.v1...

data class MensajeChat(
    val mensaje: String = "",
    val nombre_usuario: String = "",
    val uid: String = "",
    val timestamp: Any = ServerValue.TIMESTAMP // Firebase pondrá la hora del servidor
) {
    // Constructor vacío requerido por Firebase
    constructor() : this("", "", "", 0L)
}