package rules

import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper
import org.junit.rules.ExternalResource

class DatastoreRule : ExternalResource() {

    private val helper = LocalServiceTestHelper(LocalDatastoreServiceTestConfig(), LocalMemcacheServiceTestConfig())
    lateinit var datastore: DatastoreService

    override fun before() {
        helper.setUp()
        datastore = DatastoreServiceFactory.getDatastoreService()
    }

    override fun after() {
        helper.tearDown()
    }
}