package com.joom.colonist.modular;

import com.joom.colonist.modular.colony.ModularColony;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ModularColonyTest {
  private final ByteArrayOutputStream stream = new ByteArrayOutputStream();
  private final PrintStream out = System.out;

  @Before
  public void replaceOutput() {
    System.setOut(new PrintStream(stream));
  }

  @After
  public void restoreOutput() {
    System.setOut(out);
  }

  @Test
  public void testModularColonyIsProcessed() {
    final List<String> produceAndAcceptViaStaticExpectedStrings = Arrays.asList(
        "Producing via static com.joom.colonist.modular.settler.PublicSettler",
        "Accepted produced settler via static com.joom.colonist.modular.settler.PublicSettler",
        "Producing via static com.joom.colonist.modular.settler.SecondPublicSettler",
        "Accepted produced settler via static com.joom.colonist.modular.settler.SecondPublicSettler"
    );

    final List<String> produceAndForgetExpectedStrings = Arrays.asList(
        "Producing and forgetting com.joom.colonist.modular.settler.PublicSettler",
        "Producing and forgetting com.joom.colonist.modular.settler.SecondPublicSettler"
    );

    final List<String> produceAndAcceptExpectedStrings = Arrays.asList(
        "Producing com.joom.colonist.modular.settler.PublicSettler",
        "Accepted produced settler com.joom.colonist.modular.settler.PublicSettler",
        "Producing com.joom.colonist.modular.settler.SecondPublicSettler",
        "Accepted produced settler com.joom.colonist.modular.settler.SecondPublicSettler"
    );

    final List<String> acceptExpectedStrings = Arrays.asList(
        "Accepted com.joom.colonist.modular.settler.PublicSettler",
        "Accepted com.joom.colonist.modular.settler.SecondPublicSettler"
    );

    final ModularColony colony = new ModularColony();
    colony.settle();

    final String output = new String(stream.toByteArray(), Charset.forName("UTF-8"));

    shouldContainInOutput(output, produceAndAcceptViaStaticExpectedStrings);
    shouldContainInOutput(output, produceAndForgetExpectedStrings);
    shouldContainInOutput(output, produceAndAcceptExpectedStrings);
    shouldContainInOutput(output, acceptExpectedStrings);
  }

  private static void shouldContainInOutput(final String output, final List<String> expectedOutput) {
    final String combinedOutput = String.join("\n", expectedOutput);

    Assert.assertTrue(combinedOutput + " expected in " + output, output.contains(combinedOutput));
  }
}
