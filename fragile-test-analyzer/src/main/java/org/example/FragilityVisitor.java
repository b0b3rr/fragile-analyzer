package org.example;

import org.example.TestMethodMetrics;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class FragilityVisitor {

    // Имена методов-ассертов, которые считаем проверками поведения
    private static final Set<String> ASSERT_METHODS = new HashSet<>(Arrays.asList(
            "assertEquals", "assertNotEquals", "assertTrue", "assertFalse",
            "assertNull", "assertNotNull", "assertSame", "assertNotSame",
            "assertArrayEquals", "assertThat", "assertThrows", "assertDoesNotThrow"
    ));

    // Имя метода verify (Mockito). Учитываем, что может быть статически импортирован.
    private static final String VERIFY_METHOD = "verify";

    /**
     * Анализирует CompilationUnit и возвращает список метрик для каждого тестового метода.
     *
     * @param cu        разобранный файл исходного кода
     * @param className имя класса, к которому относятся методы
     * @return список метрик
     */
    public List<TestMethodMetrics> analyze(CompilationUnit cu, String className) {
        List<TestMethodMetrics> metricsList = new ArrayList<>();

        // Находим все методы с аннотацией @Test (JUnit Jupiter)
        cu.findAll(MethodDeclaration.class).forEach(method -> {
            boolean isTestMethod = method.getAnnotations().stream()
                    .anyMatch(annotation -> isTestAnnotation(annotation));
            if (isTestMethod) {
                String methodName = method.getNameAsString();
                int verifyCount = countVerifyCalls(method);
                int assertCount = countAssertCalls(method);

                double fragility = 0.0;
                int totalChecks = verifyCount + assertCount;
                if (totalChecks > 0) {
                    fragility = (double) verifyCount / totalChecks;
                }

                metricsList.add(new TestMethodMetrics(
                        className,
                        methodName,
                        verifyCount,
                        assertCount,
                        Math.round(fragility * 100.0) / 100.0 // округление до двух знаков
                ));
            }
        });

        return metricsList;
    }

    /**
     * Проверяет, является ли аннотация тестовой (org.junit.jupiter.api.Test).
     * При упрощённом подходе смотрим только имя аннотации.
     */
    private boolean isTestAnnotation(AnnotationExpr annotation) {
        String name = annotation.getNameAsString();
        // Возможные варианты: Test, org.junit.jupiter.api.Test
        return name.equals("Test") || name.endsWith(".Test");
    }

    /**
     * Подсчитывает количество вызовов verify в теле метода.
     * Учитываем как прямые вызовы verify(...), так и Mockito.verify(...).
     */
    private int countVerifyCalls(MethodDeclaration method) {
        AtomicInteger count = new AtomicInteger(0);
        method.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodCallExpr call, Void arg) {
                super.visit(call, arg);
                // Проверяем, что метод называется verify
                if (call.getNameAsString().equals(VERIFY_METHOD)) {
                    // Дополнительно можно проверить scope, но для простоты считаем любой verify
                    count.incrementAndGet();
                }
            }
        }, null);
        return count.get();
    }

    /**
     * Подсчитывает количество вызовов assert-методов в теле метода.
     */
    private int countAssertCalls(MethodDeclaration method) {
        AtomicInteger count = new AtomicInteger(0);
        method.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodCallExpr call, Void arg) {
                super.visit(call, arg);
                if (ASSERT_METHODS.contains(call.getNameAsString())) {
                    count.incrementAndGet();
                }
            }
        }, null);
        return count.get();
    }
}