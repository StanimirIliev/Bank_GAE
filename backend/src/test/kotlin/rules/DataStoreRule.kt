package rules

import com.clouway.app.datastore.NoSqlDatastoreTemplate
import com.clouway.app.datastore.core.DatastoreTemplate
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import org.junit.rules.ExternalResource

class DataStoreRule : ExternalResource() {

    private val helper = LocalServiceTestHelper(LocalDatastoreServiceTestConfig())
    lateinit var datastoreTemplate: DatastoreTemplate

    override fun before() {
        helper.setUp()
        datastoreTemplate = NoSqlDatastoreTemplate(DatastoreServiceFactory.getDatastoreService())
    }

    override fun after() {
        helper.tearDown()
    }
}