package gstorm

import groovy.sql.Sql
import gstorm.builders.*
import gstorm.metadata.ClassMetaData

class ModelClassEnhancer {
    final ClassMetaData metaData
    final Sql sql
    final boolean canAddIdBasedMethods

    ModelClassEnhancer(ClassMetaData classMetaData, Sql sql) {
        this.metaData = classMetaData
        this.sql = sql
        this.canAddIdBasedMethods = !metaData.isWithoutId()
    }

    public void enhance() {
        addStaticDmlMethods()
        addInstanceDmlMethods()
    }

    private def addStaticDmlMethods() {
        final modelMetaClass = metaData.modelClass.metaClass

        modelMetaClass.static.where = { String clause ->
            sql.rows(new SelectQueryBuilder(metaData).where(clause).build())
        }

        def selectAllQuery = new SelectQueryBuilder(metaData).build()
        def getAll = {
            sql.rows(selectAllQuery)
        }
        modelMetaClass.static.getAll = getAll
        modelMetaClass.static.all = getAll

        def getCount = { String optional_clause = null ->
            def query = new CountQueryBuilder(metaData)
            if (optional_clause) query.where(optional_clause)
            sql.firstRow(query.build()).count
        }
        modelMetaClass.static.count = getCount
        modelMetaClass.static.getCount = getCount

        if (this.canAddIdBasedMethods){
            def selectByIdQuery = new SelectQueryBuilder(metaData).byId().build()
            modelMetaClass.static.get = { id ->
                final result = sql.rows(selectByIdQuery, [id])
                (result) ? result.first() : null
            }
        }
    }

    private def addInstanceDmlMethods() {
        final modelMetaClass = metaData.modelClass.metaClass
        final fieldNames = metaData.fieldNames

        final insertQuery = new InsertQueryBuilder(metaData).build()

        if (canAddIdBasedMethods) {

            final updateQuery = new UpdateQueryBuilder(metaData).byId().build()
            final deleteQuery = new DeleteQueryBuilder(metaData).byId().build()

            if (!metaData.idField) { // add id if not already defined
                modelMetaClass.id = null
            }

            modelMetaClass.getId$ = { -> delegate.getProperty(metaData.idFieldName ?: 'id') }
            modelMetaClass.setId$ = { value -> delegate.setProperty(metaData.idFieldName ?: 'id', value) }

            modelMetaClass.save = {
                if (delegate.id$ == null) {
                    final values = fieldNames.collect { delegate.getProperty(it) }
                    final generatedIds = sql.executeInsert(insertQuery, values)
                    delegate.id$ = generatedIds[0][0] // pretty stupid way to extract it
                } else {
                    final values = fieldNames.collect { delegate.getProperty(it) } << delegate.id$
                    sql.executeUpdate(updateQuery, values)
                }
                delegate
            }

            modelMetaClass.delete = {
                if (delegate.id$ != null) {
                    sql.execute(deleteQuery, [delegate.id$])
                }
                delegate
            }

        } else {
            // as we don't have id, there is no proper way we can update existing record,
            // hence every call to save will always insert a record in db
            modelMetaClass.save = {
                final values = fieldNames.collect { delegate.getProperty(it) }
                sql.executeInsert(insertQuery, values)
                delegate
            }
        }
    }

}
