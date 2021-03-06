package solr

import com.lucidworks.spark.{SolrSQLHiveContext, SolrRelation}
import com.lucidworks.spark.util.{ConfigurationConstants, Constants}
import org.apache.spark.sql.{DataFrame, SaveMode, SQLContext}
import org.apache.spark.sql.sources.{DataSourceRegister, BaseRelation, CreatableRelationProvider, RelationProvider}

class DefaultSource extends RelationProvider with CreatableRelationProvider with DataSourceRegister {

  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): BaseRelation = {
    try {
      // Using scala case match is throwing the error scala.MatchError: org.apache.spark.sql.SQLContext@4fd80300 (of class org.apache.spark.sql.SQLContext)
      if (sqlContext.isInstanceOf[SolrSQLHiveContext]) {
        val sHiveContext = sqlContext.asInstanceOf[SolrSQLHiveContext]
        if (parameters.isDefinedAt(ConfigurationConstants.SOLR_COLLECTION_PARAM))
          sHiveContext.checkReadAccess(parameters.get(ConfigurationConstants.SOLR_COLLECTION_PARAM).get, "solr")
      }
      return new SolrRelation(parameters, sqlContext)
    } catch {
      case re: RuntimeException => throw re
      case e: Exception => throw new RuntimeException(e)
    }
  }

  override def createRelation(
      sqlContext: SQLContext,
      mode: SaveMode,
      parameters: Map[String, String],
      df: DataFrame): BaseRelation = {
    try {
      // Using scala case match is throwing the error scala.MatchError: org.apache.spark.sql.SQLContext@4fd80300 (of class org.apache.spark.sql.SQLContext)
      if (sqlContext.isInstanceOf[SolrSQLHiveContext]) {
        val sHiveContext = sqlContext.asInstanceOf[SolrSQLHiveContext]
        if (parameters.isDefinedAt(ConfigurationConstants.SOLR_COLLECTION_PARAM))
          sHiveContext.checkWriteAccess(parameters.get(ConfigurationConstants.SOLR_COLLECTION_PARAM).get, "solr")
      }

      // TODO: What to do with the saveMode?
      val solrRelation: SolrRelation = new SolrRelation(parameters, sqlContext, Some(df))
      solrRelation.insert(df, overwrite = true)
      solrRelation
    } catch {
      case re: RuntimeException => throw re
      case e: Exception => throw new RuntimeException(e)
    }
  }

  override def shortName(): String = Constants.SOLR_FORMAT
}
