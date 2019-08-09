package com.SecKillProject.service;

import com.SecKillProject.service.model.PromoModel;
import org.springframework.stereotype.Service;

public interface PromoService {

    PromoModel getPromoModelByItemId(Integer itemId);
}
