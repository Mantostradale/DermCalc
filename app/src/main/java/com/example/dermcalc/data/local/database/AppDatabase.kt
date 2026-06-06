package com.example.dermcalc.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.dermcalc.data.local.dao.DermCalcDAO
import com.example.dermcalc.data.local.entity.Paziente
import com.example.dermcalc.data.local.entity.Personale
import com.example.dermcalc.data.local.entity.Valutazione
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Personale::class, Paziente::class, Valutazione::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun DermCalcDao(): DermCalcDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "clinica_database"
                )
                    //Popolamento Iniziale del DB
                    .addCallback(AppDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
    private class AppDatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Coroutine in background perché il database non permette
            // operazioni di scrittura sul thread principale della UI
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = database.DermCalcDao()

                    // Creiazione utente all'avvio
                    val adminPrimario = Personale(
                        personaleId = 0, //ROOM in automatico incrementa gli ID
                        nome = "Davide",
                        cognome = "Plesa",
                        ruolo   = "Amministratore",
                        username = "dplesa",
                        passwordCifrata = "admin123"
                    )
                    // Query DAO
                    dao.inserisciPersonale(adminPrimario)

                    val admin2 = Personale(
                        personaleId = 0, //ROOM in automatico incrementa gli ID
                        nome = "Davide",
                        cognome = "Mantovan",
                        ruolo   = "Amministratore",
                        username = "dmantovan",
                        passwordCifrata = "admin123"
                    )
                    // Query DAO
                    dao.inserisciPersonale(admin2)

                    val dottorRossi = Personale(
                        personaleId = 0,
                        nome = "Mario",
                        cognome = "Rossi",
                        ruolo = "Dottore",
                        username = "mrossi",
                        passwordCifrata = "rossi2026"
                    )
                    dao.inserisciPersonale(dottorRossi)

                    val dottorBianchi = Personale(
                        personaleId = 0,
                        nome = "Elena",
                        cognome = "Bianchi",
                        ruolo = "Dottore",
                        username = "ebianchi",
                        passwordCifrata = "bianchi2026"
                    )
                    dao.inserisciPersonale(dottorBianchi)

                }
            }
        }
    }
}