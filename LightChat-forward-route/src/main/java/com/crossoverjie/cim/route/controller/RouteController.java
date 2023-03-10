package com.crossoverjie.LightChat.route.controller;

import com.crossoverjie.LightChat.common.enums.StatusEnum;
import com.crossoverjie.LightChat.common.exception.LightChatException;
import com.crossoverjie.LightChat.common.pojo.LightChatUserInfo;
import com.crossoverjie.LightChat.common.pojo.RouteInfo;
import com.crossoverjie.LightChat.common.res.BaseResponse;
import com.crossoverjie.LightChat.common.res.NULLBody;
import com.crossoverjie.LightChat.common.route.algorithm.RouteHandle;
import com.crossoverjie.LightChat.common.util.RouteInfoParseUtil;
import com.crossoverjie.LightChat.route.api.RouteApi;
import com.crossoverjie.LightChat.route.api.vo.req.ChatReqVO;
import com.crossoverjie.LightChat.route.api.vo.req.LoginReqVO;
import com.crossoverjie.LightChat.route.api.vo.req.P2PReqVO;
import com.crossoverjie.LightChat.route.api.vo.req.RegisterInfoReqVO;
import com.crossoverjie.LightChat.route.api.vo.res.LightChatServerResVO;
import com.crossoverjie.LightChat.route.api.vo.res.RegisterInfoResVO;
import com.crossoverjie.LightChat.route.cache.ServerCache;
import com.crossoverjie.LightChat.route.service.AccountService;
import com.crossoverjie.LightChat.route.service.CommonBizService;
import com.crossoverjie.LightChat.route.service.UserInfoCacheService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.Set;

/**
 * Function:
 *
 * @author crossoverJie
 *         Date: 22/05/2018 14:46
 * @since JDK 1.8
 */
@Controller
@RequestMapping("/")
public class RouteController implements RouteApi {
    private final static Logger LOGGER = LoggerFactory.getLogger(RouteController.class);

    @Autowired
    private ServerCache serverCache;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserInfoCacheService userInfoCacheService ;

    @Autowired
    private CommonBizService commonBizService ;

    @Autowired
    private RouteHandle routeHandle ;

    @ApiOperation("?????? API")
    @RequestMapping(value = "groupRoute", method = RequestMethod.POST)
    @ResponseBody()
    @Override
    public BaseResponse<NULLBody> groupRoute(@RequestBody ChatReqVO groupReqVO) throws Exception {
        BaseResponse<NULLBody> res = new BaseResponse();

        LOGGER.info("msg=[{}]", groupReqVO.toString());

        //???????????????????????????
        Map<Long, LightChatServerResVO> serverResVOMap = accountService.loadRouteRelated();
        for (Map.Entry<Long, LightChatServerResVO> LightChatServerResVOEntry : serverResVOMap.entrySet()) {
            Long userId = LightChatServerResVOEntry.getKey();
            LightChatServerResVO LightChatServerResVO = LightChatServerResVOEntry.getValue();
            if (userId.equals(groupReqVO.getUserId())){
                //???????????????
                LightChatUserInfo LightChatUserInfo = userInfoCacheService.loadUserInfoByUserId(groupReqVO.getUserId());
                LOGGER.warn("????????????????????? userId={}",LightChatUserInfo.toString());
                continue;
            }

            //????????????
            ChatReqVO chatVO = new ChatReqVO(userId,groupReqVO.getMsg()) ;
            accountService.pushMsg(LightChatServerResVO ,groupReqVO.getUserId(),chatVO);

        }

        res.setCode(StatusEnum.SUCCESS.getCode());
        res.setMessage(StatusEnum.SUCCESS.getMessage());
        return res;
    }


    /**
     * ????????????
     *
     * @param p2pRequest
     * @return
     */
    @ApiOperation("?????? API")
    @RequestMapping(value = "p2pRoute", method = RequestMethod.POST)
    @ResponseBody()
    @Override
    public BaseResponse<NULLBody> p2pRoute(@RequestBody P2PReqVO p2pRequest) throws Exception {
        BaseResponse<NULLBody> res = new BaseResponse();

        try {
            //???????????????????????????????????????
            LightChatServerResVO LightChatServerResVO = accountService.loadRouteRelatedByUserId(p2pRequest.getReceiveUserId());

            //p2pRequest.getReceiveUserId()==>?????????????????? userID
            ChatReqVO chatVO = new ChatReqVO(p2pRequest.getReceiveUserId(),p2pRequest.getMsg()) ;
            accountService.pushMsg(LightChatServerResVO ,p2pRequest.getUserId(),chatVO);

            res.setCode(StatusEnum.SUCCESS.getCode());
            res.setMessage(StatusEnum.SUCCESS.getMessage());

        }catch (LightChatException e){
            res.setCode(e.getErrorCode());
            res.setMessage(e.getErrorMessage());
        }
        return res;
    }


    @ApiOperation("???????????????")
    @RequestMapping(value = "offLine", method = RequestMethod.POST)
    @ResponseBody()
    @Override
    public BaseResponse<NULLBody> offLine(@RequestBody ChatReqVO groupReqVO) throws Exception {
        BaseResponse<NULLBody> res = new BaseResponse();

        LightChatUserInfo LightChatUserInfo = userInfoCacheService.loadUserInfoByUserId(groupReqVO.getUserId());

        LOGGER.info("user [{}] offline!", LightChatUserInfo.toString());
        accountService.offLine(groupReqVO.getUserId());

        res.setCode(StatusEnum.SUCCESS.getCode());
        res.setMessage(StatusEnum.SUCCESS.getMessage());
        return res;
    }

    /**
     * ???????????? LightChat server
     *
     * @return
     */
    @ApiOperation("????????????????????????")
    @RequestMapping(value = "login", method = RequestMethod.POST)
    @ResponseBody()
    @Override
    public BaseResponse<LightChatServerResVO> login(@RequestBody LoginReqVO loginReqVO) throws Exception {
        BaseResponse<LightChatServerResVO> res = new BaseResponse();

        // check server available
        String server = routeHandle.routeServer(serverCache.getServerList(),String.valueOf(loginReqVO.getUserId()));
        LOGGER.info("userName=[{}] route server info=[{}]", loginReqVO.getUserName(), server);

        RouteInfo routeInfo = RouteInfoParseUtil.parse(server);
        commonBizService.checkServerAvailable(routeInfo);

        //????????????
        StatusEnum status = accountService.login(loginReqVO);
        if (status == StatusEnum.SUCCESS) {

            //??????????????????
            accountService.saveRouteInfo(loginReqVO,server);

            LightChatServerResVO vo = new LightChatServerResVO(routeInfo);
            res.setDataBody(vo);

        }
        res.setCode(status.getCode());
        res.setMessage(status.getMessage());

        return res;
    }

    /**
     * ????????????
     *
     * @return
     */
    @ApiOperation("????????????")
    @RequestMapping(value = "registerAccount", method = RequestMethod.POST)
    @ResponseBody()
    @Override
    public BaseResponse<RegisterInfoResVO> registerAccount(@RequestBody RegisterInfoReqVO registerInfoReqVO) throws Exception {
        BaseResponse<RegisterInfoResVO> res = new BaseResponse();

        long userId = System.currentTimeMillis();
        RegisterInfoResVO info = new RegisterInfoResVO(userId, registerInfoReqVO.getUserName());
        info = accountService.register(info);

        res.setDataBody(info);
        res.setCode(StatusEnum.SUCCESS.getCode());
        res.setMessage(StatusEnum.SUCCESS.getMessage());
        return res;
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    @ApiOperation("????????????????????????")
    @RequestMapping(value = "onlineUser", method = RequestMethod.POST)
    @ResponseBody()
    @Override
    public BaseResponse<Set<LightChatUserInfo>> onlineUser() throws Exception {
        BaseResponse<Set<LightChatUserInfo>> res = new BaseResponse();

        Set<LightChatUserInfo> LightChatUserInfos = userInfoCacheService.onlineUser();
        res.setDataBody(LightChatUserInfos) ;
        res.setCode(StatusEnum.SUCCESS.getCode());
        res.setMessage(StatusEnum.SUCCESS.getMessage());
        return res;
    }





}
