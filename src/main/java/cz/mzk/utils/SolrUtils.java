package cz.mzk.utils;

import cz.mzk.constants.SolrField;
import org.apache.solr.common.SolrInputDocument;

import java.util.Collections;
import java.util.Date;
import java.util.function.Function;

public class SolrUtils {

    public static void setModify(final SolrInputDocument inputDocument, final String fieldKey, final Object fieldValue) {
        inputDocument.addField(fieldKey, Collections.singletonMap("set", fieldValue));
    }

    public static Date getCurrentTimeDate() {
        return new Date();
    }

    public static String filterQueryRootPids() {
        return "{!frange l=1 u=1 v=eq(" + SolrField.UUID +"," + SolrField.ROOT_PID + ")}";
    }

    public static QueryExpr queryBuilder() {
        return new QueryBuilder();
    }

    public interface QueryExpr {
        QueryExprChain is(final String fieldName, final Object fieldValue);
        QueryExprChain not(final String fieldName, final Object fieldValue);
        QueryExprChain notRegex(final String fieldName, final Object fieldValue);
        QueryExprChain empty(final String fieldName);
        QueryExprChain complex(final Function<QueryExpr, QueryExprChain> exprConsumer);
    }

    public interface QueryExprChain {
        QueryExpr or();
        QueryExpr and();
        String build();
    }

    public static class QueryBuilder implements QueryExpr, QueryExprChain {
        private String expr;

        public QueryBuilder() {
            expr = "";
        }

        @Override
        public QueryExprChain is(final String fieldName, final Object fieldValue) {
            expr += fieldName + ":\"" + fieldValue + "\"";
            return this;
        }

        @Override
        public QueryExprChain not(String fieldName, Object fieldValue) {
            expr += "-" + fieldName + ":\"" + fieldValue + "\"";
            return this;
        }

        @Override
        public QueryExprChain notRegex(final String fieldName, final Object fieldValue) {
            expr += "-" + fieldName + ":" + "/.*" + fieldValue + ".*/";
            return this;
        }

        @Override
        public QueryExprChain empty(String fieldName) {
            expr += "-" + fieldName + ":*";
            return this;
        }

        @Override
        public QueryExprChain complex(Function<QueryExpr, QueryExprChain> exprConsumer) {
            expr += "(" + exprConsumer.apply(new QueryBuilder()).build() + ")";
            return this;
        }

        @Override
        public String build() {
            return expr;
        }

        @Override
        public QueryExpr or() {
            expr += " OR ";
            return this;
        }

        @Override
        public QueryExpr and() {
            expr += " AND ";
            return this;
        }
    }
}
