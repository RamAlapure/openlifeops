package com.openlifeops.governance;

import com.openlifeops.core.domain.Action;
import com.openlifeops.core.domain.PolicyDecision;

public interface PolicyEngine {

    PolicyDecision evaluate(Action action);
}
