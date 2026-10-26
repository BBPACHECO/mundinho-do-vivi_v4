package com.mundodedescobertas.app

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.mundodedescobertas.app.data.PreferencesRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PreferencesRepositoryTest {
    @get:Rule val temporary=TemporaryFolder()

    @Test fun `settings survive closing and reopening the datastore`() = runTest {
        val file=temporary.newFolder().resolve("test.preferences_pb")
        var scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
        var repository=PreferencesRepository(PreferenceDataStoreFactory.create(scope=scope,produceFile={file}))
        repository.update{it.copy(childName="Bia",calmMode=true,sessionMinutes=20)}
        assertEquals("Bia",repository.preferences.first().childName)
        scope.coroutineContext[Job]!!.cancelAndJoin()
        scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
        try {
            repository=PreferencesRepository(PreferenceDataStoreFactory.create(scope=scope,produceFile={file}))
            val restored=repository.preferences.first()
            assertEquals("Bia",restored.childName)
            assertTrue(restored.calmMode)
            assertEquals(20,restored.sessionMinutes)
        } finally {scope.coroutineContext[Job]!!.cancelAndJoin()}
    }
    @Test fun `simultaneous updates do not overwrite unrelated preferences`() = runTest {
        val file=temporary.newFolder().resolve("parallel.preferences_pb")
        val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
        try {
            val repo=PreferencesRepository(PreferenceDataStoreFactory.create(scope=scope,produceFile={file}))
            coroutineScope {
                launch{repo.update{it.copy(childName="Vivi querido")}}
                launch{repo.update{it.copy(calmMode=true)}}
                launch{repo.update{it.copy(masterVolume=.3f)}}
            }
            val result=repo.preferences.first()
            assertEquals("Vivi querido",result.childName)
            assertTrue(result.calmMode)
            assertEquals(.3f,result.masterVolume,0f)
        } finally {scope.coroutineContext[Job]!!.cancelAndJoin()}
    }
}
