package com.exasol.sql.dml.merge.rendering;

import static com.exasol.hamcrest.SqlFragmentRenderResultMatcher.rendersTo;
import static com.exasol.sql.expression.BooleanTerm.eq;
import static com.exasol.sql.expression.BooleanTerm.gt;
import static com.exasol.sql.expression.ExpressionTerm.column;
import static com.exasol.sql.expression.ExpressionTerm.integerLiteral;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.exasol.sql.StatementFactory;
import com.exasol.sql.dml.merge.Merge;

// [utest->dsn~rendering.sql.merge~1]
class TestMergeRendering {
    private Merge merge;

    @BeforeEach()
    void beforeEach() {
        this.merge = StatementFactory.getInstance().mergeInto("dst");
    }

    @Test
    void testMerge() {
        assertThat(this.merge, rendersTo("MERGE INTO dst"));
    }

    @Test
    void testMergeIntoUsingOn() {
        assertThat(this.merge //
                .using("src") //
                .on(eq(column("c1", "src"), column("c1", "dst"))),
                rendersTo("MERGE INTO dst USING src ON src.c1 = dst.c1"));
    }

    @Test
    void testMergeWhenMatchedUpdate() {
        this.merge //
                .using("src") //
                .on(eq(column("c1", "src"), column("c1", "dst"))) //
                .whenMatched() //
                .thenUpdate() //
                .setToDefault("c2") //
                .set("c3", "foo") //
                .set("c4", 42);
        assertThat(this.merge, rendersTo("MERGE INTO dst USING src ON src.c1 = dst.c1" //
                + " WHEN MATCHED THEN UPDATE SET c2 = DEFAULT, c3 = 'foo', c4 = 42"));

    }

    @Test
    void testMergeWhenMatchedUpdateWhere() {
        this.merge //
                .using("src") //
                .on(eq(column("c1", "src"), column("c1", "dst"))) //
                .whenMatched() //
                .thenUpdate() //
                .setToDefault("c2") //
                .set("c3", "foo") //
                .set("c4", 42).where(gt(column("c5", "src"), integerLiteral(1000)));
        assertThat(this.merge, rendersTo("MERGE INTO dst USING src ON src.c1 = dst.c1" //
                + " WHEN MATCHED THEN UPDATE SET c2 = DEFAULT, c3 = 'foo', c4 = 42" //
                + " WHERE src.c5 > 1000"));
    }

    @Test
    void testMergeWhenMatchedDelete() {
        this.merge //
                .using("src") //
                .on(eq(column("c1", "src"), column("c1", "dst"))) //
                .whenMatched() //
                .thenDelete();
        assertThat(this.merge, rendersTo("MERGE INTO dst USING src ON src.c1 = dst.c1" //
                + " WHEN MATCHED THEN DELETE"));

    }

    @Test
    void testMergeWhenMatchedDeleteWhere() {
        this.merge //
                .using("src") //
                .on(eq(column("c1", "src"), column("c1", "dst"))) //
                .whenMatched() //
                .thenDelete() //
                .where(gt(column("c5", "src"), integerLiteral(1000)));
        assertThat(this.merge, rendersTo("MERGE INTO dst USING src ON src.c1 = dst.c1" //
                + " WHEN MATCHED THEN DELETE WHERE src.c5 > 1000"));

    }

    @Test
    void testMergeWhenNotMatchedInsertValues() {
        this.merge //
                .using("src") //
                .on(eq(column("c1", "src"), column("c1", "dst"))) //
                .whenNotMatched() //
                .thenInsert() //
                .values("foo", "bar");
        assertThat(this.merge, rendersTo("MERGE INTO dst USING src ON src.c1 = dst.c1" //
                + " WHEN NOT MATCHED THEN INSERT VALUES ('foo', 'bar')"));
    }

    @Test
    void testMergeWhenNotMatchedInsertValuesWhere() {
        this.merge //
                .using("src") //
                .on(eq(column("c1", "src"), column("c1", "dst"))) //
                .whenNotMatched() //
                .thenInsert() //
                .values("foo", "bar") //
                .where(gt(column("c5", "src"), integerLiteral(1000)));
        assertThat(this.merge, rendersTo("MERGE INTO dst USING src ON src.c1 = dst.c1" //
                + " WHEN NOT MATCHED THEN INSERT VALUES ('foo', 'bar') WHERE src.c5 > 1000"));
    }

    @Test
    void testMergeWhenNotMatchedInsertFieldValues() {
        this.merge //
                .using("src") //
                .on(eq(column("c1", "src"), column("c1", "dst"))) //
                .whenNotMatched() //
                .thenInsert() //
                .field("c3", "c4") //
                .values("foo", "bar");
        assertThat(this.merge, rendersTo("MERGE INTO dst USING src ON src.c1 = dst.c1" //
                + " WHEN NOT MATCHED THEN INSERT (c3, c4) VALUES ('foo', 'bar')"));
    }

    @Test
    void testComplexMerge() {
        final Merge complexMerge = StatementFactory.getInstance().mergeInto("dst", "t1").using("src", "t2") //
                .on(eq(column("c1", "t1"), column("c1", "t2")));
        complexMerge.whenMatched() //
                .thenUpdate() //
                .setToDefault("c2") //
                .set("c3", "foo") //
                .set("c4", 42);
        complexMerge.whenNotMatched() //
                .thenInsert() //
                .field("c3", "c5") //
                .values("foo", "bar");
        assertThat(complexMerge, rendersTo("MERGE INTO dst AS t1 USING src AS t2 ON t1.c1 = t2.c1" //
                + " WHEN MATCHED THEN UPDATE SET c2 = DEFAULT, c3 = 'foo', c4 = 42" //
                + " WHEN NOT MATCHED THEN INSERT (c3, c5) VALUES ('foo', 'bar')"));
    }
}