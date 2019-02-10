#include <iostream>
#include <string>
#include <CkHttp.h>
#include <CkHttpRequest.h>
#include <CkHttpResponse.h>

class ApiClient
{
  const std::string baseUrl = "";
public:
  
  void print() const;
};

void person::print() const
{
  std::cout << name << ":" << age << std::endl;
  /* "name" and "age" are the member variables.
     The "this" keyword is an expression whose value is the address
     of the object for which the member was invoked. Its type is 
     const person*, because the function is declared const.
 */
}

int main() {


}
