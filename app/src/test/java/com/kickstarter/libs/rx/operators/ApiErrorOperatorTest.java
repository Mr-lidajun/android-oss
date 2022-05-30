package com.kickstarter.libs.rx.operators;

import com.google.gson.Gson;
import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.services.apiresponses.ErrorEnvelope;

import org.junit.Test;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

public final class ApiErrorOperatorTest extends KSRobolectricTestCase {
  @Test
  public void testErrorResponse() {
    final Gson gson = new Gson();

    final PublishSubject<Response<Integer>> response = PublishSubject.create();
    final Observable<Integer> result = response.lift(Operators.apiError(gson));

    final TestSubscriber<Integer> resultTest = new TestSubscriber<>();
    result.subscribe(resultTest);

    response.onNext(Response.error(400, ResponseBody.create(null, "")));

    resultTest.assertNoValues();
    assertEquals(1, resultTest.getOnErrorEvents().size());
  }

  @Test
  public void testErrorResponseErrorBodyJSON() {
    final Gson gson = new Gson();

    final PublishSubject<Response<Integer>> response = PublishSubject.create();
    final Observable<Integer> result = response.lift(Operators.apiError(gson));

    final TestSubscriber<Integer> resultTest = new TestSubscriber<>();
    result.subscribe(resultTest);

    final ErrorEnvelope envelope = ErrorEnvelope.builder()
            .ksrCode(ErrorEnvelope.TFA_FAILED)
            .httpCode(400)
            .build();

    final String jsonString = new Gson().toJson(envelope);
    response.onNext(Response.error(400, ResponseBody.create(jsonString, MediaType.parse("application/json; charset=utf-8"))));

    resultTest.assertNoValues();
    assertEquals(1, resultTest.getOnErrorEvents().size());
  }

  @Test
  public void testErrorResponseBadJSON() {
    final Gson gson = new Gson();

    final PublishSubject<Response<Integer>> response = PublishSubject.create();
    final Observable<Integer> result = response.lift(Operators.apiError(gson));

    final TestSubscriber<Integer> resultTest = new TestSubscriber<>();
    result.subscribe(resultTest);
    final String message = "{malformed json}";

    final ResponseBody body= ResponseBody.create(message, MediaType.parse("application/json; charset=utf-8"));
    response.onNext(Response.error(503, body));

    resultTest.assertNoValues();
    assertEquals(1, resultTest.getOnErrorEvents().size());
  }

  @Test
  public void testResponseNull() {
    final Gson gson = new Gson();

    final PublishSubject<Response<Integer>> response = PublishSubject.create();
    final Observable<Integer> result = response.lift(Operators.apiError(gson));

    final TestSubscriber<Integer> resultTest = new TestSubscriber<>();
    result.subscribe(resultTest);

    response.onNext(null);

    resultTest.assertNoValues();
    assertEquals(1, resultTest.getOnErrorEvents().size());
  }

  @Test
  public void testNullErrorResponse() {
    final Gson gson = new Gson();

    final PublishSubject<Response<Integer>> response = PublishSubject.create();
    final Observable<Integer> result = response.lift(Operators.apiError(gson));

    final TestSubscriber<Integer> resultTest = new TestSubscriber<>();
    result.subscribe(resultTest);

    response.onError(null);

    resultTest.assertNoValues();
    assertEquals(1, resultTest.getOnErrorEvents().size());
  }

  @Test
  public void testSuccessResponse() {
    final Gson gson = new Gson();

    final PublishSubject<Response<Integer>> response = PublishSubject.create();
    final Observable<Integer> result = response.lift(Operators.apiError(gson));

    final TestSubscriber<Integer> resultTest = new TestSubscriber<>();
    result.subscribe(resultTest);

    response.onNext(Response.success(42));

    resultTest.assertValues(42);
    resultTest.assertCompleted();
  }
}
