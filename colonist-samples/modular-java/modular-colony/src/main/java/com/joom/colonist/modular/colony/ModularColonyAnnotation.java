package com.joom.colonist.modular.colony;

import com.joom.colonist.AcceptSettlersAndForget;
import com.joom.colonist.AcceptSettlersViaCallback;
import com.joom.colonist.Colony;
import com.joom.colonist.ProduceSettlersAsClasses;
import com.joom.colonist.ProduceSettlersViaCallback;
import com.joom.colonist.ProduceSettlersViaConstructor;
import com.joom.colonist.SelectSettlersByAnnotation;
import com.joom.colonist.modular.api.Settler;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Colony
@SelectSettlersByAnnotation(Settler.class)
@AcceptSettlersViaCallback
@ProduceSettlersViaConstructor
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@interface ModularColonyAnnotation {
}

@Colony
@SelectSettlersByAnnotation(Settler.class)
@AcceptSettlersViaCallback
@ProduceSettlersViaCallback
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@interface ModularColonyProduceAnnotation {
}

@Colony
@SelectSettlersByAnnotation(Settler.class)
@AcceptSettlersViaCallback
@ProduceSettlersViaCallback
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@interface ModularColonyProduceStaticAnnotation {
}

@Colony
@SelectSettlersByAnnotation(Settler.class)
@AcceptSettlersAndForget
@ProduceSettlersViaCallback
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@interface ModularColonyProduceAndForgetAnnotation {
}
