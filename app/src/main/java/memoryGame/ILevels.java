package memoryGame;


public interface ILevels extends IConstants{

    public enum Level {

        LEVEL1(LEVEL_1, LEVEL1_TIME , ROWS_LEVEL1 , ROWS_LEVEL1*COLS_NUM),
        LEVEL2(LEVEL_2, LEVEL2_TIME , ROWS_LEVEL2 , ROWS_LEVEL2*COLS_NUM),
        LEVEL3(LEVEL_3, LEVEL3_TIME , ROWS_LEVEL3 , ROWS_LEVEL3*COLS_NUM);

        private int maxTime;
        private int rowsNum;
        private int colsNum;
        private int cardsNum;
        private int pairsNum;
        private int num;

        Level(int num,int time, int rowNum, int cardsNum) {
            this.num = num;
            this.maxTime = time;
            this.rowsNum = rowNum;
            this.cardsNum = cardsNum;

            pairsNum = this.cardsNum / PAIR ;
            colsNum = COLS_NUM;
        }

        public int maxTime() {
            return maxTime;
        }

        int rowsNum(){
            return  rowsNum;
        }

        public int pairsNum(){
            return pairsNum;
        }

        int colsNum(){
            return colsNum;
        }

        int num(){
            return num;
        }

    }
}
