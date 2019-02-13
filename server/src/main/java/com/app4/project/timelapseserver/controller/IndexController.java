package com.app4.project.timelapseserver.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

  @GetMapping("/")
  public String index() {
    return "Go to \n\tapi/ for api end point and\n\tstorage/ for storage endpoint";
  }

}
