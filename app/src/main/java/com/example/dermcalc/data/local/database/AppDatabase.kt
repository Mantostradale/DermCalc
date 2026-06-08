package com.example.dermcalc.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.dermcalc.data.local.dao.DermCalcDAO
import com.example.dermcalc.data.local.entity.DatiBiometrici
import com.example.dermcalc.data.local.entity.DatiEASI
import com.example.dermcalc.data.local.entity.DatiPASI
import com.example.dermcalc.data.local.entity.Paziente
import com.example.dermcalc.data.local.entity.Personale
import com.example.dermcalc.data.local.entity.Valutazione
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Personale::class, Paziente::class, Valutazione::class,
        DatiPASI::class, DatiEASI::class, DatiBiometrici::class],
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
                    .addCallback(AppDatabaseCallback { INSTANCE!! }) // Uso lambda
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val databaseProvider: () -> AppDatabase  // Riceve una lambda
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val dao = databaseProvider().DermCalcDao() // Chiama la lambda solo qui

                dao.inserisciPersonale(Personale(
                    personaleId = 0,
                    nome = "Davide",
                    cognome = "Plesa",
                    ruolo = "Amministratore",
                    username = "dplesa",
                    passwordCifrata = "admin123"
                ))
                dao.inserisciPersonale(Personale(
                    personaleId = 0,
                    nome = "Davide",
                    cognome = "Mantovan",
                    ruolo = "Amministratore",
                    username = "dmantovan",
                    passwordCifrata = "admin123"
                ))
                dao.inserisciPersonale(Personale(
                    personaleId = 0,
                    nome = "Mario",
                    cognome = "Rossi",
                    ruolo = "Dottore",
                    username = "mrossi",
                    passwordCifrata = "rossi2026"
                ))
                dao.inserisciPersonale(Personale(
                    personaleId = 0,
                    nome = "Elena",
                    cognome = "Bianchi",
                    ruolo = "Dottore",
                    username = "ebianchi",
                    passwordCifrata = "bianchi2026"
                ))
                dao.inserisciPaziente(Paziente(
                    pazienteId = 0,
                    personaleIdResponsabile = 4,
                    nome = "Francesco",
                    cognome = "Mariani",
                    codiceFiscale = "MRNFNC84M12F205Z",
                    dataNascita = "1984-08-12"
                ))
            }
        }
    }
}