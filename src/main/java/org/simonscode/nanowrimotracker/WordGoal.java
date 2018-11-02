package org.simonscode.nanowrimotracker;

class WordGoal {

    private String name = "";
    private int targetWordcount = 0;
    private Type type = Type.FIXED;
    private int lastReach = 0;

    WordGoal() {

    }

    WordGoal(String name, int targetWordcount, Type type) {
        this.name = name;
        this.targetWordcount = targetWordcount;
        this.type = type;
    }

    boolean hasBeenReached(int currentWordcount) {
        if (type == Type.FIXED && currentWordcount >= targetWordcount) {
            lastReach = currentWordcount;
            return true;
        } else if (type == Type.REPEATING && currentWordcount >= targetWordcount + lastReach) {
            while (currentWordcount >= targetWordcount + lastReach) {
                lastReach += targetWordcount;
            }
            return true;
        }
        return false;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    int getTargetWordcount() {
        return targetWordcount;
    }

    void setTargetWordcount(int targetWordcount) {
        this.targetWordcount = targetWordcount;
    }

    Type getType() {
        return type;
    }

    void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        FIXED,
        REPEATING,
        NONE,
    }

    String getCompletionMessage() {
        switch (type) {
            case FIXED:
                return name;
            case REPEATING:
                return String.format("%s %d times", name, lastReach / targetWordcount);
            case NONE:
            default:
                return "Error";
        }
    }
}
