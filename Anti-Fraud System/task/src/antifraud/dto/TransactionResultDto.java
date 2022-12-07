package antifraud.dto;

public class TransactionResultDto {
    private String result;
    private String info;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "TransactionResultDto{" +
                "result='" + result + '\'' +
                ", info='" + info + '\'' +
                '}';
    }
}
