package pushmanager.comment;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author chenxu
 * @date 19-6-19 - 下午2:20
 * @apiNote Description app Verson Update manager
 */
@Data
@ApiModel
public class BaseResponseDto<T> implements Serializable {

    private static final long serialVersionUID = 8837009487120956221L;

    @ApiModelProperty(value = "状态码")
    private Integer code;

    @ApiModelProperty("返回数据")
    private T data;

    @ApiModelProperty("错误信息")
    private Object msg;

    private long timeStr = System.currentTimeMillis();

    public BaseResponseDto() {
    }

    public BaseResponseDto(Integer code, Object msg) {
        this.code = code;
        this.msg = msg;
    }

    public BaseResponseDto(Integer code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public static BaseResponseDto<Object> success(Object data) {
        BaseResponseDto<Object> responseDto = new BaseResponseDto<>(200,
                data, "操作成功");
        return responseDto;
    }

    public static BaseResponseDto<Object> success(int code, Object data) {
        BaseResponseDto<Object> responseDto = new BaseResponseDto<>(code,
                data, "操作成功");
        return responseDto;
    }

    public static BaseResponseDto success(int code, Object data, String msg) {
        BaseResponseDto<Object> responseDto = new BaseResponseDto<>(code,
                data, msg);
        return responseDto;
    }

    public static BaseResponseDto<Object> successUpload(Object data) {
        return new BaseResponseDto<Object>(200,
                data, "操作成功");
    }
    public static BaseResponseDto<Object> error(Object errorMessage) {
        BaseResponseDto responseDto = new BaseResponseDto(400, errorMessage);
        return responseDto;
    }


    public static BaseResponseDto<Object> logs(Object message) {
        BaseResponseDto responseDto = new BaseResponseDto(-100, message);
        return responseDto;
    }


    /**
     * 进度条的返回code值 和消息
     * @param message
     * @return
     */
    public static BaseResponseDto<Object> progress(Object message) {
        BaseResponseDto responseDto = new BaseResponseDto(50, message);
        return responseDto;
    }


    public static BaseResponseDto<Object> error(int code, String errorMessage) {
        BaseResponseDto responseDto = new BaseResponseDto(code, errorMessage);
        return responseDto;
    }

    public static BaseResponseDto<Object> error(int code, Object data, String errorMessage) {
        BaseResponseDto responseDto = new BaseResponseDto(code, data, errorMessage);
        return responseDto;
    }

    public static BaseResponseDto<Object> success() {
        return success(null);
    }

}
