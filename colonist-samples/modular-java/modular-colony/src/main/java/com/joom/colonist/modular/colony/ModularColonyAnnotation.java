package com.joom.colonist.modular.colony;

import com.joom.colonist.AcceptSettlersViaCallback;
import com.joom.colonist.Colony;
import com.joom.colonist.ProduceSettlersAsClasses;
import com.joom.colonist.SelectSettlersByAnnotation;
import com.joom.colonist.modular.api.Settler;

@Colony
@SelectSettlersByAnnotation(Settler.class)
@AcceptSettlersViaCallback
@ProduceSettlersAsClasses
@interface ModularColonyAnnotation {
}
