package com.control.porcinochl

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CerdaDao {

    // --------- Inserción y Actualización ---------
    @Query("SELECT * FROM cerdas WHERE id IN (:ids)")
    suspend fun getCerdasByIds(ids: List<String>): List<Cerda>

    @Query("""
        SELECT * FROM cerdas 
        WHERE 
            date(fechaCelo / 1000, 'unixepoch') = date('now') OR
            date(fechaParto / 1000, 'unixepoch') = date('now', '+10 days') OR
            date(fechaDestete / 1000, 'unixepoch') = date('now')
    """)
    suspend fun obtenerCerdasConEventosProximos(): List<Cerda>

    @Query("SELECT * FROM cerdas WHERE id LIKE :queryId LIMIT :limit OFFSET :offset")
    suspend fun buscarPorIdPaginado(queryId: String, limit: Int, offset: Int): List<Cerda>

    @Query("SELECT * FROM cerdas WHERE id LIKE :queryId")
    suspend fun buscarPorId(queryId: String): List<Cerda>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(cerda: Cerda)

    @Update
    suspend fun actualizar(cerda: Cerda)

    @Delete
    suspend fun eliminar(cerda: Cerda)

    // --------- Consultas únicas ---------

    @Query("SELECT * FROM cerdas WHERE id = :id")
    suspend fun getCerdaById(id: String): Cerda?

    // --------- Consultas múltiples ---------

    @Query("SELECT * FROM cerdas")
    suspend fun obtenerTodas(): List<Cerda>

    @Query("SELECT * FROM cerdas")
    fun obtenerTodasLiveData(): LiveData<List<Cerda>>

    @Query("SELECT * FROM cerdas")
    fun obtenerTodasFlow(): Flow<List<Cerda>>
}
