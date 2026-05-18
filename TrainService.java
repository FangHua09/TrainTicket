package service;

import concurrent.StockManager;
import entity.Train;

import java.util.ArrayList;
import java.util.List;

/**
 * 车次服务，负责车次初始化和库存管理。
 */
public class TrainService {

    /** 库存管理器 */
    private final StockManager stockManager = new StockManager();
    /** 座位类型列表 */
    private static final List<String> SEAT_CLASSES = List.of("二等座", "一等座", "商务座");
    /** 车次编号前缀 */
    private static final String TRAIN_NO_PREFIX = "G100";

    /**
     * 获取库存管理器。
     * @return StockManager实例
     */
    public StockManager getStockManager() {
        return stockManager;
    }

    /**
     * 获取座位类型列表。
     * @return 座位类型列表
     */
    public List<String> seatClasses() {
        return SEAT_CLASSES;
    }

    /**
     * 获取所有车次编号（包含座位类型）。
     * @return 车次编号列表（格式：G1001-二等座）
     */
    public List<String> trainNos() {
        List<String> trainNos = new ArrayList<String>();
        for (int i = 1; i <= 5; i++) {
            String baseNo = TRAIN_NO_PREFIX + i;
            // 每个车次包含三种座位类型
            for (String seatClass : SEAT_CLASSES) {
                trainNos.add(baseNo + "-" + seatClass);
            }
        }
        return trainNos;
    }

    /**
     * 初始化演示车次数据。
     */
    public void initializeDemoTrains() {
        stockManager.clear();  // 先清空现有数据
        
        String[][] routes = {
            {"北京", "上海"},
            {"上海", "广州"},
            {"广州", "深圳"},
            {"北京", "广州"},
            {"上海", "杭州"}
        };
        
        int[] seatCounts = {100, 50, 20};  // 二等座100张，一等座50张，商务座20张
        
        // 为每个车次创建三种座位类型的库存
        for (int i = 0; i < 5; i++) {
            String trainNo = TRAIN_NO_PREFIX + (i + 1);
            String departure = routes[i][0];
            String destination = routes[i][1];
            
            for (int j = 0; j < SEAT_CLASSES.size(); j++) {
                Train train = new Train(
                        trainNo + "-" + SEAT_CLASSES.get(j),  // 车次编号包含座位类型
                        departure,
                        destination,
                        SEAT_CLASSES.get(j),
                        seatCounts[j]  // 座位数
                );
                stockManager.registerTrain(train);
            }
        }
    }

    /**
     * 添加新的车次。
     * @param trainNo 车次编号
     * @param departure 出发站
     * @param destination 到达站
     * @param seatClass 座位类型
     * @param totalTickets 总票数
     */
    public void addTrain(String trainNo, String departure, String destination, 
                         String seatClass, int totalTickets) {
        Train train = new Train(trainNo, departure, destination, seatClass, totalTickets);
        stockManager.registerTrain(train);
    }
}