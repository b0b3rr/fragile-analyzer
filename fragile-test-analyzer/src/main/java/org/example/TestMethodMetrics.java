package org.example;

public class TestMethodMetrics {
    private String className;
    private String methodName;
    private int verifyCount;
    private int assertCount;
    private double fragilityIndex;

    public TestMethodMetrics() {
    }

    public TestMethodMetrics(String className, String methodName, int verifyCount, int assertCount, double fragilityIndex) {
        this.className = className;
        this.methodName = methodName;
        this.verifyCount = verifyCount;
        this.assertCount = assertCount;
        this.fragilityIndex = fragilityIndex;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getVerifyCount() {
        return verifyCount;
    }

    public void setVerifyCount(int verifyCount) {
        this.verifyCount = verifyCount;
    }

    public int getAssertCount() {
        return assertCount;
    }

    public void setAssertCount(int assertCount) {
        this.assertCount = assertCount;
    }

    public double getFragilityIndex() {
        return fragilityIndex;
    }

    public void setFragilityIndex(double fragilityIndex) {
        this.fragilityIndex = fragilityIndex;
    }
}