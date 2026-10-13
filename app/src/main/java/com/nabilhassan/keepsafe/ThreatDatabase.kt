package com.nabilhassan.keepsafe

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking

@Entity(tableName = "malicious_urls")
data class MaliciousUrl(
    @PrimaryKey val url: String,
    val domain: String?,
    val source: String,
    val threatType: String = "phishing"
)

@Entity(tableName = "scan_history")
data class ScanHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val host: String,
    val score: Int,
    val verdictAr: String,
    val verdictEn: String,
    val scanType: String = "URL",
    val scannedAt: Long = System.currentTimeMillis()
)

@Dao
interface ThreatDao {
    @Query("SELECT * FROM malicious_urls WHERE url = :url LIMIT 1")
    suspend fun findByUrl(url: String): MaliciousUrl?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(urls: List<MaliciousUrl>)
}

@Dao
interface ScanHistoryDao {
    @Query("SELECT * FROM scan_history ORDER BY scanned_at DESC")
    fun getAll(): Flow<List<ScanHistory>>

    @Query("SELECT * FROM scan_history ORDER BY scanned_at DESC")
    suspend fun getAllOnce(): List<ScanHistory>

    @Insert
    suspend fun insert(item: ScanHistory)

    @Query("DELETE FROM scan_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM scan_history")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM scan_history")
    fun getTotalScans(): Flow<Int>

    @Query("SELECT COUNT(*) FROM scan_history WHERE score >= 50")
    fun getDangerousScans(): Flow<Int>

    @Query("SELECT COUNT(*) FROM scan_history WHERE score <= 20")
    fun getSafeScans(): Flow<Int>

    @Query("SELECT AVG(score) FROM scan_history")
    fun getAverageScore(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM scan_history WHERE scan_type = :type")
    fun getScansByType(type: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM scan_history WHERE scanned_at >= :since")
    fun getScansSince(since: Long): Flow<Int>
}

@Database(
    entities = [MaliciousUrl::class, ScanHistory::class],
    version = 1,
    exportSchema = false
)
abstract class ThreatDatabase : RoomDatabase() {
    abstract fun threatDao(): ThreatDao
    abstract fun scanHistoryDao(): ScanHistoryDao

    fun isMalicious(url: String): MaliciousUrl? = runBlocking {
        threatDao().findByUrl(url)
    }

    companion object {
        @Volatile private var INSTANCE: ThreatDatabase? = null

        fun getInstance(context: Context): ThreatDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ThreatDatabase::class.java,
                    "keep_safe.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
