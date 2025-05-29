import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
export  class HelloWorldBen {
  constructor(
    public message: string
  ) {}
}
@Injectable({
  providedIn: 'root'
})

export class WelcomeDataService {
  constructor(private http: HttpClient) { }

  executeHelloWorldBenService(): Observable<any> {
    return this.http.get<HelloWorldBen>('http://localhost:8080/api/hello-world');
  }
  executeHelloWorldServiceWithPathVariable(name: string): Observable<any> {
    return this.http.get<HelloWorldBen>(`http://localhost:8080/api/hello-world/${name}`);
  }
}
