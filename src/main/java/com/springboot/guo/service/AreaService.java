package com.springboot.guo.service;

import com.springboot.guo.bean.ConsultConfigArea;

import java.util.List;
import java.util.Map;

public interface AreaService {
    public List<ConsultConfigArea> qryAreaFromBase(Map param);
    
    public List<ConsultConfigArea> qryArea(Map param);
    
    public int saveArea(ConsultConfigArea area);
    
    public int saveAreaToBase(ConsultConfigArea area);

}
