package com.example.teqmed.domain.error

/**
 * Jerarquia unica de errores del sistema (tolerancia cero a crash).
 *
 * Extiende Exception a proposito: asi encaja en el Result<T> que ya devuelven
 * la calculadora y los repositorios, sin reescribir sus firmas.
 *
 * REGLA: estos errores NUNCA se lanzan con throw. Se envuelven en
 * Result.failure(...) y el ViewModel los traduce a texto del UiState.
 * De esa forma un fallo se muestra en pantalla en vez de cerrar la app.
 *
 * mensajeUsuario es lo que ve el tecnico en terreno: en castellano,
 * sin nombres de clase ni stack traces.
 */
sealed class ErrorDominio(
    val mensajeUsuario: String,
    causaOriginal: Throwable? = null
) : Exception(mensajeUsuario, causaOriginal) {

    /** Si es true, el usuario puede corregir y reintentar sin ayuda. */
    open val esRecuperable: Boolean = true


    // ─────────── Reglas de negocio: RN01 a RN05 ───────────

    sealed class Negocio(mensaje: String) : ErrorDominio(mensaje) {

        /** La solicitud de calculo llego incompleta (RN02). */
        data class DatosIncompletos(val detalle: String) : Negocio(
            "No se puede valorizar el traslado: $detalle"
        )

        /** RN01: el rendimiento en km/L del vehiculo es 0 o negativo. */
        data class RendimientoInvalido(val patente: String) : Negocio(
            "El vehiculo $patente no tiene un rendimiento valido en km/L. " +
                    "Avise al administrador."
        )

        /** El odometro final no es mayor al inicial. */
        data class OdometroInconsistente(
            val inicial: Double,
            val finalLeido: Double
        ) : Negocio(
            "El kilometraje final (${finalLeido.toInt()}) debe ser mayor " +
                    "al inicial (${inicial.toInt()})."
        )

        /** RN04: declaro peaje pero no fotografio el comprobante. */
        object EvidenciaPeajeFaltante : Negocio(
            "Debe fotografiar el comprobante del peaje antes de cerrar el traslado."
        )

        /** El destino no tiene distancia oficial tabulada (RN03 no es auditable). */
        data class DistanciaOficialNoDefinida(val destino: String) : Negocio(
            "El destino $destino no tiene distancia oficial registrada. " +
                    "Avise al administrador."
        )

        /** RF-16: faltan los parametros vigentes para calcular. */
        object TarifasNoVigentes : Negocio(
            "No hay tarifas vigentes cargadas. El costo se calculara al sincronizar."
        )

        /** El total dio cero o negativo: hay un dato corrupto. */
        data class CostoInvalido(val total: Double) : Negocio(
            "El costo calculado ($total) no es valido. Revise los datos ingresados."
        )

        /** Transicion de estado no permitida en la maquina de estados. */
        data class TransicionNoPermitida(
            val estadoActual: String,
            val accion: String
        ) : Negocio(
            "No se puede $accion un traslado en estado $estadoActual."
        )
    }


    // ─────────── Sesion y permisos: EP01 ───────────

    sealed class Sesion(mensaje: String) : ErrorDominio(mensaje) {

        object CredencialesInvalidas : Sesion(
            "Correo o clave incorrectos."
        )

        data class UsuarioInactivo(val nombre: String) : Sesion(
            "La cuenta de $nombre esta desactivada. Contacte al administrador."
        )

        /** RF-37: el rol del usuario no concede el permiso solicitado. */
        data class PermisoDenegado(val permiso: String) : Sesion(
            "Su perfil no tiene autorizacion para esta accion."
        ) {
            override val esRecuperable = false
        }

        object SesionExpirada : Sesion(
            "La sesion se cerro. Ingrese nuevamente."
        )
    }


    // ─────────── Persistencia local: Room ───────────

    sealed class Datos(
        mensaje: String,
        causa: Throwable? = null
    ) : ErrorDominio(mensaje, causa) {

        data class RegistroNoEncontrado(val entidad: String) : Datos(
            "No se encontro el $entidad solicitado."
        )

        /** Patente o RUT repetido: la PK ya existe. */
        data class ClaveDuplicada(val valor: String) : Datos(
            "El registro $valor ya existe en el sistema."
        )

        data class FalloEscritura(val causaReal: Throwable?) : Datos(
            "No se pudo guardar la informacion en el dispositivo.",
            causaReal
        )

        data class FalloLectura(val causaReal: Throwable?) : Datos(
            "No se pudo leer la informacion guardada.",
            causaReal
        )
    }


    // ─────────── Recursos nativos: GPS y camara ───────────

    sealed class Recurso(mensaje: String) : ErrorDominio(mensaje) {

        object PermisoUbicacionDenegado : Recurso(
            "Se necesita acceso a la ubicacion para sellar el inicio del traslado. " +
                    "Actívelo en los ajustes del telefono."
        )

        object UbicacionNoDisponible : Recurso(
            "No se pudo obtener la ubicacion. Verifique que el GPS este encendido."
        )

        object PermisoCamaraDenegado : Recurso(
            "Se necesita acceso a la camara para fotografiar el comprobante."
        )

        object FotoNoGuardada : Recurso(
            "No se pudo guardar la fotografia. Intente nuevamente."
        )
    }


    // ─────────── Sincronizacion: RN05, operacion offline ───────────

    sealed class Sincronizacion(mensaje: String) : ErrorDominio(mensaje) {

        /** No es un error bloqueante: el traslado queda en la cola local. */
        object SinConexion : Sincronizacion(
            "Sin conexion. El traslado quedo guardado y se enviara automaticamente."
        )

        data class FalloEnvio(val cantidadPendiente: Int) : Sincronizacion(
            "No se pudieron enviar $cantidadPendiente registros. Se reintentara."
        )
    }


    // ─────────── Red de seguridad ───────────

    /** Cualquier cosa no prevista. Existe para que nada llegue como crash. */
    data class Inesperado(val causaReal: Throwable?) : ErrorDominio(
        "Ocurrio un problema inesperado. Si persiste, informe a soporte.",
        causaReal
    ) {
        override val esRecuperable = false
    }
}