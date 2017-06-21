package core;

import entity.AnalyzedData;
import entity.Point;
import enumeration.Color;
import helper.MapDriver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class LevelProcessor {

    private static boolean debug = false;

    private static int[] sequenceWeight = {0, 1, 10, 100, 1000, 10000};

    static Color win(GameMap gameMap) {
        for (int i = 0; i < Config.size; i++)
            for (int j = 0; j < Config.size; j++) {
                Point point = new Point(i, j);
                if (gameMap.getColor(point) != Color.NULL) {
                    for (int direct = 0; direct < 4; direct++) {
                        Color color = gameMap.getColor(point);
                        if (gameMap.checkColors(color, point, direct, 0, 4)) {
                            return gameMap.getColor(point);
                        }
                    }
                }
            }
        return null;
    }

    static List<Point> getExpandPoints(GameMap gameMap, Color color, int level) {
        AnalyzedData data = gameMap.getAnalyzedPoints(color);
        List<Point> result = selectSet(data, level);

        if (result.isEmpty() && level > Config.searchDeep.getValue() - Config.fullDeep) {
            result.add(new Point(7, 7));
            return result;
        }

        if (result.size() == data.getOrigin().size()) {
            int left = result.size() - data.getNotKey().size();
            int right = result.size();


            List<Integer> score = new ArrayList<>(result.size());
            for (int i = 0; i < left; i++) {
                score.add(0);
            }
            for (int i = left; i < right; i++) {
                try {
                    score.add(getScore(gameMap, result.get(i)));
                } catch (RuntimeException e) {
                    System.out.println("look");
                }
            }

            if (!result.isEmpty()) {
                sort(left, right, result, score);
            }
        }

        if (debug) {
            printResult(result);
        }
        return result;
    }

    private static List<Point> selectSet(AnalyzedData data, int level) {
        //如果能连5，则连5
        if (!data.getFiveAttack().isEmpty()) {
            return new ArrayList<>(data.getFiveAttack());
        }
        //如果有对方冲4，则防冲4
        if (!data.getFourDefence().isEmpty()) {
            return new ArrayList<>(data.getFourDefence());
        }
        //如果有对方活3，则防活3或者冲四
        if (!data.getThreeDefence().isEmpty()) {
            return new ArrayList<Point>(data.getFourAttack()) {{
                addAll(data.getThreeDefence());
            }};
        }
        if (level > Config.searchDeep.getValue() - Config.fullDeep) {
            List<Point> result = new ArrayList<>();
            result.addAll(data.getFiveAttack());
            result.addAll(data.getFourAttack());
            result.addAll(data.getFourDefence());
            result.addAll(data.getThreeDefence());
            result.addAll(data.getThreeOpenAttack());
            result.addAll(data.getNotKey());
            return result;
        } else {
            List<Point> points = new ArrayList<>();
            points.addAll(data.getThreeOpenAttack());
            points.addAll(data.getFourAttack());
            return new ArrayList<>(new HashSet<>(points));
        }
    }

    private static int getScore(GameMap gameMap, Point point) {
        int value = 0;
        for (int i = 0; i < 8; i++) {
            Point fresh = gameMap.getRelatePoint(point, i, 1);
            if (gameMap.reachable(fresh)) {
                Color color = gameMap.getColor(fresh);
                if (color != Color.NULL) {
                    int length = gameMap.getMaxSequence(color, fresh, i);
                    if (length > 5) {
                        length = 5;
                    }
                    value += sequenceWeight[length];
                }
            }
        }
        return value;
    }

    private static void sort(int left, int right, List<Point> points, List<Integer> scores) {
        for (int x = left; x < right; x++)
            for (int y = x + 1; y < right; y++)
                if (scores.get(x) <= scores.get(y)) {
                    int temp = scores.get(x);
                    scores.set(x, scores.get(y));
                    scores.set(y, temp);
                    Point point = points.get(x);
                    points.set(x, points.get(y));
                    points.set(y, point);
                }
    }

    private static void printResult(List<Point> result) {
        result.forEach(point -> {
            System.out.println(point.getX() + " " + point.getY());
        });
        System.out.printf(String.valueOf(result.size()));
    }

    public static void main(String[] args) {
        Color[][] map = MapDriver.readMap();
        GameMap gameMap = new GameMap(map);

        AnalyzedData points = gameMap.getAnalyzedPoints(Color.BLACK);
        System.out.println(points.getFiveAttack());
        System.out.println(points.getFourAttack());
        System.out.println(points.getThreeOpenAttack());
        System.out.println(points.getFourDefence());
        System.out.println(points.getThreeDefence());
    }
}
