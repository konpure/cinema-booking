package com.cinema.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SentinelConfig {

    @PostConstruct
    public void initFlowRules() {
        FlowRule lockRule = new FlowRule("bookingLock");
        lockRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        lockRule.setCount(10);

        FlowRule submitRule = new FlowRule("bookingSubmit");
        submitRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        submitRule.setCount(5);

        List<FlowRule> rules = Arrays.asList(lockRule, submitRule);
        FlowRuleManager.loadRules(rules);
    }
}
